package com.qertyy.gamblingmod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.qertyy.gamblingmod.network.RequestSpinPayload;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.List;

public class RouletteTabScreen {

    private final MyRouletteScreen parent;
    private final RouletteManager manager = RouletteManager.getInstance();
    private Button spinButton;

    public RouletteTabScreen(MyRouletteScreen parent) {
        this.parent = parent;
    }

    public void initSub(Minecraft mc, int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;

        this.spinButton = Button.builder(Component.literal("Крутить рулетку!"), button -> {
            if (!manager.isSpinning()) {
                PacketDistributor.sendToServer(new RequestSpinPayload());
            }
        }).bounds(centerX - 60, centerY + 55, 120, 20).build();
    }

    public void addSubWidgets() {
        this.parent.addWidgetFromSub(this.spinButton);
    }

    public void clearSubWidgets() {
        this.parent.removeWidgetFromSub(this.spinButton);
    }

    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int viewWidth = 220;
        int viewHeight = 44;
        int viewX = (this.parent.width - viewWidth) / 2;
        int viewY = (this.parent.height - viewHeight) / 2 - 15;

        graphics.fill(viewX - 4, viewY - 4, viewX + viewWidth + 4, viewY + viewHeight + 4, 0xFF222222);
        graphics.fill(viewX, viewY, viewX + viewWidth, viewY + viewHeight, 0xFF111111);

        List<ItemStack> items = manager.getRouletteItems();
        double currentScrollX = manager.getCurrentScrollX();

        graphics.enableScissor(viewX, viewY, viewX + viewWidth, viewY + viewHeight);
        for (int i = 0; i < items.size(); i++) {
            double itemX = viewX + (i * RouletteManager.SLOT_STEP) - currentScrollX + (viewWidth / 2.0) - (RouletteManager.ITEM_SIZE / 2.0);
            if (itemX + RouletteManager.ITEM_SIZE > viewX && itemX < viewX + viewWidth) {
                ItemStack stack = items.get(i);
                graphics.fill((int) itemX - 2, viewY + 4, (int) itemX + RouletteManager.ITEM_SIZE + 2, viewY + viewHeight - 4, 0xFF2A2A2A);

                graphics.pose().pushPose();
                graphics.pose().translate(itemX, viewY + 6, 0);
                graphics.pose().scale(2.0f, 2.0f, 1.0f);
                graphics.renderItem(stack, 0, 0);
                graphics.pose().popPose();
            }
        }
        graphics.disableScissor();

        int centerX = this.parent.width / 2;
        graphics.fill(centerX - 1, viewY - 6, centerX + 1, viewY + viewHeight + 6, 0xFFFF0000);
        graphics.drawCenteredString(Minecraft.getInstance().font, "Рулетка", this.parent.width / 2, viewY - 20, 0xFFD700);
    }
}
