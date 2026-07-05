package io.github.scottbass3.simplehatscollection.client;

import fonnymunkey.simplehats.common.item.HatItem;
import io.github.scottbass3.simplehatscollection.core.Platform;
import io.github.scottbass3.simplehatscollection.data.HatCollection;
import io.github.scottbass3.simplehatscollection.data.StoredHat;
import io.github.scottbass3.simplehatscollection.hat.HatSupport;
import io.github.scottbass3.simplehatscollection.net.RequestSyncPayload;
import io.github.scottbass3.simplehatscollection.net.WithdrawHatPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The collection screen: a scrollable grid with one cell per base hat. Unlocked hats
 * show their icon and a stored count; missing hats are greyed out. Clicking an unlocked
 * hat with a single stored variant withdraws it; hats with several stored variants open
 * a small pick-list so the player chooses which variant/dye to take back.
 */
public class CollectionScreen extends Screen {

    private static final int COLS = 9;
    private static final int CELL = 18;
    private static final int VISIBLE_ROWS = 6;
    private static final int GRID_W = COLS * CELL;
    private static final int GRID_H = VISIBLE_ROWS * CELL;

    private final List<HatItem> hats = new ArrayList<>();
    private int gridLeft;
    private int gridTop;
    private int scrollRow;
    private int maxScrollRow;

    // Open variant pick-list ("context menu"), by hat name; null when closed.
    private String openMenuName;
    private final List<HatCollection.Counted> menuRows = new ArrayList<>();
    private int menuLeft;
    private int menuTop;
    private int menuWidth;

    public CollectionScreen() {
        super(Component.translatable("screen.simplehatscollection.title"));
    }

    @Override
    protected void init() {
        this.hats.clear();
        this.hats.addAll(HatSupport.allHats());
        this.hats.sort(Comparator
                .comparingInt((HatItem h) -> h.getHatEntry().getHatRarity().ordinal())
                .thenComparing(h -> h.getHatEntry().getHatName()));

        int totalRows = Math.max(1, (this.hats.size() + COLS - 1) / COLS);
        this.maxScrollRow = Math.max(0, totalRows - VISIBLE_ROWS);
        this.scrollRow = Math.min(this.scrollRow, this.maxScrollRow);

        this.gridLeft = (this.width - GRID_W) / 2;
        this.gridTop = 44;

        // Refresh the collection from the server whenever the screen opens.
        Platform.SEND_TO_SERVER.accept(RequestSyncPayload.INSTANCE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        HatCollection collection = ClientCollectionData.get();

        // Panel + title.
        int panelPad = 8;
        graphics.fill(gridLeft - panelPad, gridTop - 24,
                gridLeft + GRID_W + panelPad, gridTop + GRID_H + panelPad, 0xC0101010);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, gridTop - 18, 0xFFFFFF);

        int unlocked = 0;
        for (HatItem h : hats) if (collection.totalForName(h.getHatEntry().getHatName()) > 0) unlocked++;
        graphics.drawString(this.font,
                Component.translatable("screen.simplehatscollection.progress", unlocked, hats.size())
                        .withStyle(ChatFormatting.GRAY),
                gridLeft - panelPad + 2, gridTop + GRID_H + panelPad - 10, 0xFFFFFF);

        HatItem hovered = null;
        int hoveredX = 0, hoveredY = 0;

        graphics.enableScissor(gridLeft, gridTop, gridLeft + GRID_W, gridTop + GRID_H);
        for (int vr = 0; vr < VISIBLE_ROWS; vr++) {
            for (int c = 0; c < COLS; c++) {
                int index = (scrollRow + vr) * COLS + c;
                if (index >= hats.size()) continue;
                HatItem hat = hats.get(index);
                String name = hat.getHatEntry().getHatName();
                int total = collection.totalForName(name);

                int x = gridLeft + c * CELL;
                int y = gridTop + vr * CELL;
                graphics.fill(x, y, x + CELL, y + CELL, 0x20FFFFFF);

                ItemStack icon = firstStoredOrBase(hat, collection);
                graphics.renderItem(icon, x + 1, y + 1);
                if (total <= 0) {
                    graphics.fill(x + 1, y + 1, x + 17, y + 17, 0xB0101010);
                } else if (total > 1) {
                    graphics.renderItemDecorations(this.font, icon, x + 1, y + 1, String.valueOf(total));
                }

                if (mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                    hovered = hat;
                    hoveredX = x;
                    hoveredY = y;
                }
            }
        }
        graphics.disableScissor();

        if (openMenuName != null) {
            renderMenu(graphics, mouseX, mouseY, collection);
        } else if (hovered != null) {
            graphics.renderComponentTooltip(this.font, buildTooltip(hovered, collection), mouseX, mouseY);
            // Remember the hovered cell as the anchor for a potential pick-list.
            this.menuLeft = hoveredX;
            this.menuTop = hoveredY;
        }
    }

    private List<Component> buildTooltip(HatItem hat, HatCollection collection) {
        List<Component> lines = new ArrayList<>();
        lines.add(new ItemStack(hat).getHoverName());
        String name = hat.getHatEntry().getHatName();
        int total = collection.totalForName(name);
        if (total <= 0) {
            lines.add(Component.translatable("tooltip.simplehatscollection.missing")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            lines.add(Component.translatable("tooltip.simplehatscollection.stored", total)
                    .withStyle(ChatFormatting.GREEN));
            List<HatCollection.Counted> variants = collection.variantsForName(name);
            if (variants.size() > 1) {
                lines.add(Component.translatable("tooltip.simplehatscollection.click_variants")
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                lines.add(Component.translatable("tooltip.simplehatscollection.click_withdraw")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }
        return lines;
    }

    private void renderMenu(GuiGraphics graphics, int mouseX, int mouseY, HatCollection collection) {
        menuRows.clear();
        menuRows.addAll(collection.variantsForName(openMenuName));
        if (menuRows.isEmpty()) {
            openMenuName = null;
            return;
        }

        this.menuWidth = 120;
        int rowH = 18;
        int height = menuRows.size() * rowH + 4;
        int left = Math.min(this.menuLeft, this.width - menuWidth - 2);
        int top = Math.min(this.menuTop, this.height - height - 2);
        graphics.fill(left, top, left + menuWidth, top + height, 0xF0000000);
        graphics.fill(left, top, left + menuWidth, top + 1, 0xFFAAAAAA);

        for (int i = 0; i < menuRows.size(); i++) {
            HatCollection.Counted counted = menuRows.get(i);
            int ry = top + 2 + i * rowH;
            boolean rowHover = mouseX >= left && mouseX < left + menuWidth && mouseY >= ry && mouseY < ry + rowH;
            if (rowHover) graphics.fill(left + 1, ry, left + menuWidth - 1, ry + rowH, 0x40FFFFFF);

            ItemStack stack = counted.hat().toStack();
            graphics.renderItem(stack, left + 2, ry + 1);
            Component label = Component.translatable("screen.simplehatscollection.variant",
                    counted.hat().variant(), counted.count());
            graphics.drawString(this.font, label, left + 22, ry + 5, 0xFFFFFF);
        }
    }

    private static ItemStack firstStoredOrBase(HatItem hat, HatCollection collection) {
        List<HatCollection.Counted> variants = collection.variantsForName(hat.getHatEntry().getHatName());
        if (!variants.isEmpty()) return variants.get(0).hat().toStack();
        return new ItemStack(hat);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (openMenuName != null) {
            int rowH = 18;
            int height = menuRows.size() * rowH + 4;
            int left = Math.min(this.menuLeft, this.width - menuWidth - 2);
            int top = Math.min(this.menuTop, this.height - height - 2);
            if (mouseX >= left && mouseX < left + menuWidth && mouseY >= top && mouseY < top + height) {
                int i = (int) ((mouseY - top - 2) / rowH);
                if (i >= 0 && i < menuRows.size()) {
                    Platform.SEND_TO_SERVER.accept(new WithdrawHatPayload(menuRows.get(i).hat()));
                    return true;
                }
            }
            openMenuName = null; // click outside closes the pick-list
            return true;
        }

        HatItem clicked = cellAt(mouseX, mouseY);
        if (clicked != null && button == 0) {
            HatCollection collection = ClientCollectionData.get();
            String name = clicked.getHatEntry().getHatName();
            List<HatCollection.Counted> variants = collection.variantsForName(name);
            if (variants.isEmpty()) {
                return true; // missing hat, nothing to withdraw
            } else if (variants.size() == 1) {
                Platform.SEND_TO_SERVER.accept(new WithdrawHatPayload(variants.get(0).hat()));
                return true;
            } else {
                this.openMenuName = name; // anchor was set while hovering during render
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private HatItem cellAt(double mouseX, double mouseY) {
        if (mouseX < gridLeft || mouseX >= gridLeft + GRID_W || mouseY < gridTop || mouseY >= gridTop + GRID_H) {
            return null;
        }
        int c = (int) ((mouseX - gridLeft) / CELL);
        int vr = (int) ((mouseY - gridTop) / CELL);
        int index = (scrollRow + vr) * COLS + c;
        if (index < 0 || index >= hats.size()) return null;
        return hats.get(index);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            this.scrollRow = Math.max(0, Math.min(this.maxScrollRow, this.scrollRow - (int) Math.signum(scrollY)));
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
