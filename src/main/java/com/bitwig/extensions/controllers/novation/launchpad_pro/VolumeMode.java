package com.bitwig.extensions.controllers.novation.launchpad_pro;

import com.bitwig.extension.controller.api.Parameter;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;

public class VolumeMode extends Mode
{
   public VolumeMode(final LaunchpadProControllerExtension driver)
   {
      super(driver, "volume");

      mShiftLayer = new LaunchpadLayer(driver, "volume-shift");

      final TrackBank trackBank = driver.getTrackBank();
      for (int x = 0; x < 8; ++x)
      {
         final Track track = trackBank.getItemAt(x);
         final Parameter volume = track.volume();
         for (int y = 0; y < 8; ++y)
         {
            final double padValue = y / 7.0;
            final Button button = driver.getPadButton(x, y);
            bindPressed(button, () -> volume.setImmediately(padValue));
            bindLightState(() -> {
               final double value = volume.get();
               if (!track.exists().get())
                  return LedState.OFF;
               if (value >= padValue)
                  return new LedState(track.color());
               return LedState.OFF;
            }, button.getLight());
         }
      }

      bindLightState(LedState.VOLUME_MODE, driver.getVolumeButton());

      bindLightState(() -> trackBank.canScrollForwards().get() ? LedState.TRACK : LedState.TRACK_LOW, driver.getRightButton());
      bindLightState(() -> trackBank.canScrollBackwards().get() ? LedState.TRACK : LedState.TRACK_LOW, driver.getLeftButton());
      bindLightState(() -> LedState.OFF, driver.getDownButton());
      bindLightState(() -> LedState.OFF, driver.getUpButton());

      bindPressed(driver.getRightButton(), trackBank.scrollForwardsAction());
      bindPressed(driver.getLeftButton(), trackBank.scrollBackwardsAction());
      mShiftLayer.bindPressed(driver.getRightButton(), trackBank.scrollPageForwardsAction());
      mShiftLayer.bindPressed(driver.getLeftButton(), trackBank.scrollPageBackwardsAction());
   }

   @Override
   protected String getModeDescription()
   {
      return "Volume";
   }

   @Override
   protected void doActivate()
   {
      final TrackBank trackBank = mDriver.getTrackBank();

      for (int i = 0; i < 8; ++i)
      {
         final Track track = trackBank.getItemAt(i);
         track.subscribe();
         track.volume().subscribe();
         track.volume().setIndication(true);
         track.color().subscribe();
      }
   }

   @Override
   protected void doDeactivate()
   {
      mShiftLayer.deactivate();
      final TrackBank trackBank = mDriver.getTrackBank();

      for (int i = 0; i < 8; ++i)
      {
         final Track track = trackBank.getItemAt(i);
         track.volume().unsubscribe();
         track.volume().setIndication(false);
         track.color().unsubscribe();
         track.unsubscribe();
      }
   }

   @Override
   void updateKeyTranslationTable(final Integer[] table)
   {
      for (int i = 0; i < 128; ++i)
         table[i] = -1;
   }

   private final LaunchpadLayer mShiftLayer;
}
