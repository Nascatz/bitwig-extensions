package com.bitwig.extensions.controllers.arturia.keylab.mk2;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import com.bitwig.extension.api.Color;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.SettableBooleanValue;
import com.bitwig.extension.controller.api.TriggerAction;
import com.bitwig.extensions.framework.Layers;

class Layer extends com.bitwig.extensions.framework.Layer
{
   public Layer(final Layers layers, final String name)
   {
      super(layers, name);

      mExtension = (ArturiaKeylabMkII)layers.getControllerExtension();
   }

   public void bindPressed(final ButtonId buttonId, final TriggerAction action)
   {
      bindPressed(button(buttonId), action);
   }

   public void bindPressed(final ButtonId buttonId, final Runnable action)
   {
      bindPressed(button(buttonId), action);
   }

   public void bindToggle(final ButtonId buttonId, final SettableBooleanValue value)
   {
      bindToggle(button(buttonId), value);
   }

   public void bindToggle(final ButtonId buttonId, final Layer value)
   {
      bindToggle(button(buttonId), value);
   }

   public void bindToggle(
      final ButtonId button,
      final TriggerAction pressedAction,
      final BooleanSupplier isLightOnOffSupplier)
   {
      bindToggle(button(button), pressedAction, isLightOnOffSupplier);
   }

   public void bind(final Supplier<Color> color, final ButtonId buttonId)
   {
      bind(color, button(buttonId));
   }

   public void bind(final BooleanSupplier isOn, final ButtonId buttonId)
   {
      bind(isOn, button(buttonId));
   }

   private HardwareButton button(final ButtonId buttonId)
   {
      return mExtension.getButton(buttonId);
   }

   private final ArturiaKeylabMkII mExtension;
}