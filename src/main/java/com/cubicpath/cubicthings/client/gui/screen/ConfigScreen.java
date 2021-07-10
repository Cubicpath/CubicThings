////////////////////////////////////////////////////////////////////////////////
//                    MIT Licence (C) 2021 Cubicpath@Github                    /
////////////////////////////////////////////////////////////////////////////////

package com.cubicpath.cubicthings.client.gui.screen;

import com.cubicpath.cubicthings.CubicThings;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.ParametersAreNonnullByDefault;

public class ConfigScreen extends Screen {
    protected Screen prevScreen;

    protected Button cancelButton;

    public ConfigScreen(Screen prevScreen) {
        super(ITextComponent.getTextComponentOrEmpty(""));
        this.prevScreen = prevScreen;
    }

    @Override
    protected void init() {
        super.init();

        this.cancelButton = addButton(new Button(0, 0,40, 20, new TranslationTextComponent("gui.cancel"), (button) -> {
            getMinecraft().displayGuiScreen(this.prevScreen);
        }));

        int i = 0;
        for (String key : CubicThings.Config.SPEC.valueMap().keySet()){
            Object value = CubicThings.Config.SPEC.valueMap().get(key);
            CubicThings.LOGGER.info(CubicThings.Config.SPEC.entrySet().size());

            i++;
            if (i != 0)
                addButton(new Button(40, 20 + (40 * ++i), 60, 20, ITextComponent.getTextComponentOrEmpty(key), (button) -> {
                }));
        }

    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
