package me.shedaniel.reiclothconfig2.gui.entries;

import com.google.common.collect.Lists;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class IntegerSliderEntry extends TooltipListEntry<Integer> {
    
    protected Slider sliderWidget;
    protected GuiButton resetButton;
    protected AtomicInteger value;
    private int minimum, maximum;
    private Consumer<Integer> saveConsumer;
    private Supplier<Integer> defaultValue;
    private Function<Integer, String> textGetter = integer -> String.format("Value: %d", integer);
    private List<IGuiEventListener> widgets;
    
    @Deprecated
    public IntegerSliderEntry(String fieldName, int minimum, int maximum, int value, Consumer<Integer> saveConsumer) {
        this(fieldName, minimum, maximum, value, "text.cloth-config.reset_value", null, saveConsumer);
    }
    
    @Deprecated
    public IntegerSliderEntry(String fieldName, int minimum, int maximum, int value, String resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer) {
        this(fieldName, minimum, maximum, value, resetButtonKey, defaultValue, saveConsumer, null);
    }
    
    @Deprecated
    public IntegerSliderEntry(String fieldName, int minimum, int maximum, int value, String resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier) {
        this(fieldName, minimum, maximum, value, resetButtonKey, defaultValue, saveConsumer, tooltipSupplier, false);
    }
    
    @Deprecated
    public IntegerSliderEntry(String fieldName, int minimum, int maximum, int value, String resetButtonKey, Supplier<Integer> defaultValue, Consumer<Integer> saveConsumer, Supplier<Optional<String[]>> tooltipSupplier, boolean requiresRestart) {
        super(fieldName, tooltipSupplier, requiresRestart);
        this.defaultValue = defaultValue;
        this.value = new AtomicInteger(value);
        this.saveConsumer = saveConsumer;
        this.maximum = maximum;
        this.minimum = minimum;
        this.sliderWidget = new Slider(0, 0, 152, 20, ((double) this.value.get() - minimum) / Math.abs(maximum - minimum));
        this.resetButton = new GuiButton(31231, 0, 0, Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(resetButtonKey)) + 6, 20, I18n.format(resetButtonKey)) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                sliderWidget.setProgress((MathHelper.clamp(IntegerSliderEntry.this.defaultValue.get(), minimum, maximum) - minimum) / (double) Math.abs(maximum - minimum));
                IntegerSliderEntry.this.value.set(MathHelper.clamp(IntegerSliderEntry.this.defaultValue.get(), minimum, maximum));
                sliderWidget.updateMessage();
                getScreen().setEdited(true, isRequiresRestart());
            }
        };
        this.sliderWidget.displayString = (textGetter.apply(IntegerSliderEntry.this.value.get()));
        this.widgets = Lists.newArrayList(sliderWidget, resetButton);
    }
    
    @Override
    public void save() {
        if (saveConsumer != null)
            saveConsumer.accept(getValue());
    }
    
    public Function<Integer, String> getTextGetter() {
        return textGetter;
    }
    
    public IntegerSliderEntry setTextGetter(Function<Integer, String> textGetter) {
        this.textGetter = textGetter;
        this.sliderWidget.displayString = (textGetter.apply(IntegerSliderEntry.this.value.get()));
        return this;
    }
    
    @Override
    public Integer getValue() {
        return value.get();
    }
    
    @Override
    public Optional<Integer> getDefaultValue() {
        return defaultValue == null ? Optional.empty() : Optional.ofNullable(defaultValue.get());
    }
    
    @Override
    public List<? extends IGuiEventListener> getChildren() {
        return widgets;
    }
    
    public IntegerSliderEntry setMaximum(int maximum) {
        this.maximum = maximum;
        return this;
    }
    
    public IntegerSliderEntry setMinimum(int minimum) {
        this.minimum = minimum;
        return this;
    }
    
    @Override
    public void render(int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
        super.render(index, y, x, entryWidth, entryHeight, mouseX, mouseY, isSelected, delta);
        MainWindow window = Minecraft.getInstance().mainWindow;
        this.resetButton.enabled = isEditable() && getDefaultValue().isPresent() && defaultValue.get().intValue() != value.get();
        this.resetButton.y = y;
        this.sliderWidget.enabled = isEditable();
        this.sliderWidget.y = y;
        if (Minecraft.getInstance().fontRenderer.getBidiFlag()) {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), window.getScaledWidth() - x - Minecraft.getInstance().fontRenderer.getStringWidth(I18n.format(getFieldName())), y + 5, 16777215);
            this.resetButton.x = x;
            this.sliderWidget.x = x + resetButton.getWidth() + 1;
            this.sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
        } else {
            Minecraft.getInstance().fontRenderer.drawStringWithShadow(I18n.format(getFieldName()), x, y + 5, 16777215);
            this.resetButton.x = x + entryWidth - resetButton.getWidth();
            this.sliderWidget.x = x + entryWidth - 150;
            this.sliderWidget.setWidth(150 - resetButton.getWidth() - 2);
        }
        resetButton.render(mouseX, mouseY, delta);
        sliderWidget.render(mouseX, mouseY, delta);
    }
    
    private class Slider extends GuiButton {
        public boolean dragging;
        private double sliderValue;
        
        protected Slider(int int_1, int int_2, int int_3, int int_4, double double_1) {
            super(new Random().nextInt(), int_1, int_2, int_3, int_4, "");
            sliderValue = double_1;
            updateMessage();
        }
        
        public void updateMessage() {
            displayString = (textGetter.apply(IntegerSliderEntry.this.value.get()));
        }
        
        protected void applyValue() {
            IntegerSliderEntry.this.value.set((int) (minimum + Math.abs(maximum - minimum) * sliderValue));
            getScreen().setEdited(true, isRequiresRestart());
        }
        
        protected int getHoverState(boolean mouseOver) {
            return 0;
        }
        
        @Override
        public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
            if (!isEditable())
                return false;
            return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }
        
        public final void onClick(double mouseX, double mouseY) {
            this.sliderValue = (mouseX - (double) (this.x + 4)) / (double) (this.width - 8);
            this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0D, 1.0D);
            applyValue();
            updateMessage();
            this.dragging = true;
        }
        
        /**
         * Called when the left mouse button is released. This method is specific to GuiButton.
         */
        public void onRelease(double mouseX, double mouseY) {
            this.dragging = false;
        }
        
        public double getProgress() {
            return sliderValue;
        }
        
        public void setProgress(double integer) {
            this.sliderValue = integer;
        }
        
        protected void renderBg(Minecraft mc, int mouseX, int mouseY) {
            if (this.visible) {
                if (this.dragging) {
                    this.sliderValue = (double) ((float) (mouseX - (this.x + 4)) / (float) (this.width - 8));
                    this.sliderValue = MathHelper.clamp(this.sliderValue, 0.0D, 1.0D);
                }
                
                mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (double) (this.width - 8)), this.y, 0, 66, 4, 20);
                this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (double) (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
            }
        }
    }
    
}
