package com.bitwig.extensions.controllers.novation.launchpad_pro;

import com.bitwig.extension.controller.api.SettableBooleanValue;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

class SoloOverlay extends Overlay
{
   public SoloOverlay(final LaunchpadProControllerExtension driver)
   {
      super(driver, "solo");

      final TrackBank trackBank = driver.getTrackBank();
      for (int x = 0; x < 8; ++x)
      {
         final Track track = trackBank.getItemAt(x);
         final Button button = driver.getPadButton(x, 0);
         bindPressed(button, track.solo().toggleAction());
         bindLightState(() -> {
            if (track.exists().get())
               return track.solo().get() ? LedState.SOLO : LedState.SOLO_LOW;
            return LedState.OFF;
         }, button);
      }

      bindLightState(LedState.SOLO, driver.getSoloButton());
   }

   @Override
   protected void doActivate()
   {
      final TrackBank trackBank = mDriver.getTrackBank();
      for (int i = 0; i < 8; ++i)
         trackBank.getItemAt(i).solo().subscribe();
   }

   @Override
   protected void doDeactivate()
   {
      final TrackBank trackBank = mDriver.getTrackBank();
      for (int i = 0; i < 8; ++i)
         trackBank.getItemAt(i).solo().unsubscribe();
   }
}
