package net.nyrfps.uix;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class NyrControlWidget extends AbstractWidget {

    private static final int KIND_BUTTON = 0;
    private static final int KIND_BOOL = 1;
    private static final int KIND_INT = 2;

    private static final int BG = 0xFF141C26;
    private static final int BG_HOVER = 0xFF1B2531;
    private static final int BORDER = 0xFF223040;
    private static final int ACCENT = 0xFF4FA8FF;
    private static final int TEXT = 0xFFEAF2F7;
    private static final int OFF = 0xFF3A4552;
    private static final int ON = 0xFF3ECF6E;

    private final String label;
    private final int kind;

    private Runnable action;

    private Supplier<Boolean> boolGet;
    private Consumer<Boolean> boolSet;

    private IntSupplier intGet;
    private IntConsumer intSet;

    private int min;
    private int max;
    private int step;

    private boolean dragging;

    public NyrControlWidget(
            int x,
            int y,
            int width,
            int height,
            String label,
            Runnable action
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal(label)
        );

        this.label = label;
        this.kind = KIND_BUTTON;
        this.action = action;
    }

    public NyrControlWidget(
            int x,
            int y,
            int width,
            int height,
            String label,
            Supplier<Boolean> boolGet,
            Consumer<Boolean> boolSet
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal(label)
        );

        this.label = label;
        this.kind = KIND_BOOL;
        this.boolGet = boolGet;
        this.boolSet = boolSet;
    }

    public NyrControlWidget(
            int x,
            int y,
            int width,
            int height,
            String label,
            IntSupplier intGet,
            IntConsumer intSet,
            int min,
            int max,
            int step
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.literal(label)
        );

        this.label = label;
        this.kind = KIND_INT;
        this.intGet = intGet;
        this.intSet = intSet;
        this.min = min;
        this.max = max;
        this.step = Math.max(1, step);
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor gfx,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int x = getX();
        int y = getY();
        int w = this.width;
        int h = this.height;

        boolean hovered =
                mouseX >= x &&
                mouseX <= x + w &&
                mouseY >= y &&
                mouseY <= y + h;

        gfx.fill(
                x,
                y,
                x + w,
                y + h,
                hovered ? BG_HOVER : BG
        );

        gfx.fill(
                x,
                y,
                x + w,
                y + 1,
                BORDER
        );

        gfx.fill(
                x,
                y + h - 1,
                x + w,
                y + h,
                BORDER
        );

        Minecraft mc = Minecraft.getInstance();

        switch (kind) {

            case KIND_BUTTON -> {
                int textWidth = mc.font.width(label);

                gfx.text(
                        mc.font,
                        label,
                        x + (w - textWidth) / 2,
                        y + (h - 8) / 2,
                        hovered ? ACCENT : TEXT,
                        false
                );
            }

            case KIND_BOOL -> {
                boolean on = boolGet != null && boolGet.get();

                gfx.text(
                        mc.font,
                        label,
                        x + 10,
                        y + (h - 8) / 2,
                        TEXT,
                        false
                );

                int pillW = 34;
                int pillH = 14;

                int px = x + w - pillW - 10;
                int py = y + (h - pillH) / 2;

                gfx.fill(
                        px,
                        py,
                        px + pillW,
                        py + pillH,
                        on ? ON : OFF
                );

                int knob = 10;

                int kx = on
                        ? px + pillW - knob - 2
                        : px + 2;

                gfx.fill(
                        kx,
                        py + 2,
                        kx + knob,
                        py + pillH - 2,
                        TEXT
                );
            }

            case KIND_INT -> {
                int value =
                        intGet != null
                                ? intGet.getAsInt()
                                : min;

                String valueText =
                        String.valueOf(value);

                gfx.text(
                        mc.font,
                        label,
                        x + 10,
                        y + 6,
                        TEXT,
                        false
                );

                gfx.text(
                        mc.font,
                        valueText,
                        x + w
                                - mc.font.width(valueText)
                                - 10,
                        y + 6,
                        ACCENT,
                        false
                );

                int barX = x + 10;
                int barY = y + h - 9;
                int barW = w - 20;
                int barH = 4;

                gfx.fill(
                        barX,
                        barY,
                        barX + barW,
                        barY + barH,
                        OFF
                );

                float t =
                        max > min
                                ? (float) (value - min)
                                / (float) (max - min)
                                : 0.0f;

                t = Math.max(
                        0.0f,
                        Math.min(1.0f, t)
                );

                int fillW =
                        Math.round(barW * t);

                if (fillW > 0) {
                    gfx.fill(
                            barX,
                            barY,
                            barX + fillW,
                            barY + barH,
                            ACCENT
                    );
                }
            }
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        output.add(
                NarratedElementType.TITLE,
                Component.literal(label)
        );
    }

    @Override
    public void onClick(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        switch (kind) {

            case KIND_BUTTON -> {
                if (action != null) {
                    action.run();
                }
            }

            case KIND_BOOL -> {
                if (boolGet != null && boolSet != null) {
                    boolSet.accept(
                            !boolGet.get()
                    );
                }
            }

            case KIND_INT -> {
                dragging = true;
                setFromMouse(event.x());
            }
        }
    }

    @Override
    protected void onDrag(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        if (kind == KIND_INT && dragging) {
            setFromMouse(event.x());
        }
    }

    @Override
    public void onRelease(
            MouseButtonEvent event
    ) {
        dragging = false;
    }

    private void setFromMouse(
            double mouseX
    ) {
        if (intSet == null) {
            return;
        }

        double t =
                (mouseX - getX())
                        / (double) this.width;

        t = Math.max(
                0.0,
                Math.min(1.0, t)
        );

        int raw =
                min
                        + (int) Math.round(
                                t * (max - min)
                        );

        int stepped =
                Math.round(
                        raw / (float) step
                ) * step;

        int clamped =
                Math.max(
                        min,
                        Math.min(max, stepped)
                );

        intSet.accept(clamped);
    }
}
