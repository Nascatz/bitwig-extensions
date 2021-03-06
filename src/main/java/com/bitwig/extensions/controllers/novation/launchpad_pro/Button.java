package com.bitwig.extensions.controllers.novation.launchpad_pro;

import com.bitwig.extension.controller.api.AbsoluteHardwareKnob;
import com.bitwig.extension.controller.api.ColorValue;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.HardwareButton;
import com.bitwig.extension.controller.api.HardwareSurface;
import com.bitwig.extension.controller.api.InternalHardwareLightState;
import com.bitwig.extension.controller.api.MidiIn;
import com.bitwig.extension.controller.api.MultiStateHardwareLight;
import com.bitwig.extension.controller.api.ObjectHardwareProperty;

class Button
{

   static final int NO_PULSE = 0;
   static final int PULSE_PLAYING = 88;
   static final int PULSE_RECORDING = 72;
   static final int PULSE_PLAYBACK_QUEUED = 89;
   static final int PULSE_RECORDING_QUEUED = 56;
   static final int PULSE_STOP_QUEUED = 118;

   enum State
   {
      RELEASED, PRESSED, HOLD,
   }

   Button(
      final LaunchpadProControllerExtension driver,
      final String id,
      final MidiIn midiIn,
      final int index,
      final boolean isPressureSensitive,
      final int x,
      final int y)
   {
      assert index >= 0;
      assert index < 100;

      mDriver = driver;

      final HardwareSurface hardwareSurface = driver.getHardwareSurface();
      mIsPressureSensitive = isPressureSensitive;
      mIndex = index;

      final HardwareButton bt = hardwareSurface.createHardwareButton(id);
      bt.isPressed().markInterested();

      if (isPressureSensitive)
      {
         bt.pressedAction().setPressureActionMatcher(midiIn.createNoteOnVelocityValueMatcher(0, index));
         bt.releasedAction().setActionMatcher(midiIn.createNoteOffActionMatcher(0, index));

         mAfterTouch = hardwareSurface.createAbsoluteHardwareKnob(id + "-at");
         mAfterTouch.setAdjustValueMatcher(midiIn.createPolyAftertouchValueMatcher(0, index));
         bt.setAftertouchControl(mAfterTouch);
      }
      else
      {
         bt.pressedAction().setActionMatcher(midiIn.createCCActionMatcher(0, index, 127));
         bt.releasedAction().setActionMatcher(midiIn.createCCActionMatcher(0, index, 0));

         mAfterTouch = null;
      }

      final MultiStateHardwareLight light = hardwareSurface.createMultiStateHardwareLight(id + "-light");
      light.state().setValue(LedState.OFF);
      light.state().onUpdateHardware(internalHardwareLightState -> mDriver.updateButtonLed(Button.this));
      bt.setBackgroundLight(light);

      mButton = bt;
      mLight = light;
      mX = x;
      mY = y;
   }

   public HardwareButton getButton()
   {
      return mButton;
   }

   public AbsoluteHardwareKnob getAfterTouch()
   {
      return mAfterTouch;
   }

   State getButtonState()
   {
      return mButtonState;
   }

   void onButtonPressed(final ControllerHost host)
   {
      mButtonState = State.PRESSED;

      mCancelHoldTask = false;
      final int HOLD_DELAY_MS = 250;
      host.scheduleTask(() -> {
         if (!mCancelHoldTask && mButtonState == State.PRESSED)
            mButtonState = State.HOLD;
      }, HOLD_DELAY_MS);
   }

   void onButtonReleased()
   {
      mButtonState = State.RELEASED;
      mCancelHoldTask = false;
   }

   boolean isPressed()
   {
      return mButtonState == State.PRESSED || mButtonState == State.HOLD;
   }

   public int getX()
   {
      return mX;
   }

   public int getY()
   {
      return mY;
   }

   public void appendLedUpdate(
      final StringBuilder ledClear, final StringBuilder ledUpdate, final StringBuilder ledPulseUpdate)
   {
      final ObjectHardwareProperty<InternalHardwareLightState> state = mLight.state();
      LedState currentState = (LedState)state.currentValue();
      final LedState lastSent = (LedState)state.lastSentValue();

      if (currentState == null)
         currentState = LedState.OFF;

      if (lastSent != null && currentState.equals(lastSent))
         return;

      final Color color = currentState.getColor();
      final int pulse = currentState.getPulse();

      if (pulse == NO_PULSE)
      {
         if (color.isBlack())
            ledClear.append(String.format(" %02x 00", mIndex));
         else
            ledUpdate.append(String.format(" %02x %02x %02x %02x", mIndex, color.getRed(), color.getGreen(), color.getBlue()));
      }
      else
         ledPulseUpdate.append(String.format(" %02x %02x", mIndex, pulse));
   }

   public MultiStateHardwareLight getLight()
   {
      return mLight;
   }

   public boolean isPressureSensitive()
   {
      return mIsPressureSensitive;
   }

   // For debugging
   @Override
   public String toString()
   {
      return mButton.getId() + " [" + mX + "; " + mY + "]";
   }

   private final LaunchpadProControllerExtension mDriver;

   /* Hardware objects */
   private final HardwareButton mButton;
   private final MultiStateHardwareLight mLight;
   private final AbsoluteHardwareKnob mAfterTouch;

   /* State */
   private final int mX;
   private final int mY;
   private final int mIndex;
   private final boolean mIsPressureSensitive;

   private State mButtonState = State.RELEASED;
   private boolean mCancelHoldTask = false;
}
