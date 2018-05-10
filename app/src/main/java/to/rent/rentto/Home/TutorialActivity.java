/*
 * Copyright 2016 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package to.rent.rentto.Home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.matthewtamlin.sliding_intro_screen_library.background.BackgroundManager;
import com.matthewtamlin.sliding_intro_screen_library.background.ColorBlender;
import com.matthewtamlin.sliding_intro_screen_library.buttons.IntroButton;
import com.matthewtamlin.sliding_intro_screen_library.core.IntroActivity;
import com.matthewtamlin.sliding_intro_screen_library.transformers.MultiViewParallaxTransformer;
import java.util.ArrayList;
import java.util.Collection;
import to.rent.rentto.R;

public class TutorialActivity extends IntroActivity {
    private static final int[] BACKGROUND_COLORS = {0xff304FFE, 0xffcc0066, 0xff9900ff, 0xff304FFE, 0xffcc0066, 0xff9900ff};

    /**
     * Name of the shared preferences which hold a key for preventing the intro screen from
     * displaying again once completed.
     */
    public static final String DISPLAY_ONCE_PREFS = "display_only_once_spfile";

    /**
     * Key to use in {@code DISPLAY_ONCE_PREFS} to prevent the intro screen from displaying again
     * once completed.
     */
    public static final String DISPLAY_ONCE_KEY = "display_only_once_spkey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBar); // Looks good when the status bar is hidden
        super.onCreate(savedInstanceState);

        // Skip to the next Activity if the user has previously completed the introduction
        if (introductionCompletedPreviously()) {
            final Intent nextActivity = new Intent(this, HomeActivity.class);
            startActivity(nextActivity);
        }
        configureTransformer();
        //configureBackground();
    }

    /**
     * Called by {@link #onCreate(Bundle)} to generate the pages displayed in this activity. The
     * returned Collection is copied, so further changes to the collection will have no effect after
     * this method returns. The total ordering of the returned collection is maintained in the
     * display of the pages.
     *
     * @param savedInstanceState
     * 		if this activity is being re-initialized after previously being shut down, then this Bundle
     * 		contains the data this activity most recently saved in {@link
     * 		#onSaveInstanceState(Bundle)}, otherwise null
     * @return the pages to display in the Activity, not null
     */
    @Override
    protected Collection<Fragment> generatePages(Bundle savedInstanceState) {
        // This variable holds the pages while they are being created
        final ArrayList<Fragment> pages = new ArrayList<>();
//        for(int i = 0; i < BACKGROUND_COLORS.length; i++) {
        for(int i = 0; i < 9; i++) {
            pages.add(WelcomeFragment.newInstance(i));
        }

        return pages;
    }

    /**
     * Called by {@link #onCreate(Bundle)} to generate the Behaviour of the final button. The {@link
     * IntroButton} class contains Behaviours which suit most needs. The Behaviour of the final
     * button can be changed later using {@link #getFinalButtonAccessor()}.
     *
     * @return the Behaviour to use for the final button, not null
     */
    @Override
    protected IntroButton.Behaviour generateFinalButtonBehaviour() {
        /* The pending changes to the shared preferences editor will be applied when the
         * introduction is successfully completed. By setting a flag in the pending edits and
         * checking the status of the flag when the activity starts, the introduction screen can
         * be skipped if it has previously been completed.
         */
        final SharedPreferences sp = getSharedPreferences(DISPLAY_ONCE_PREFS, MODE_PRIVATE);
        final SharedPreferences.Editor pendingEdits = sp.edit().putBoolean(DISPLAY_ONCE_KEY, true);

        // Define the next activity intent and create the Behaviour to use for the final button
        final Intent nextActivity = new Intent(this, HomeActivity.class);
        return new IntroButton.ProgressToNextActivity(nextActivity, pendingEdits);
    }

    /**
     * Checks for a shared preference flag indicating that the introduction has been completed
     * previously.
     *
     * @return true if the introduction has been completed before, false otherwise
     */
    private boolean introductionCompletedPreviously() {
        final SharedPreferences sp = getSharedPreferences(DISPLAY_ONCE_PREFS, MODE_PRIVATE);
        return false && sp.getBoolean(DISPLAY_ONCE_KEY, false);
    }

    /**
     * Sets this IntroActivity to use a MultiViewParallaxTransformer page transformer.
     */
    private void configureTransformer() {
        final MultiViewParallaxTransformer transformer = new MultiViewParallaxTransformer();
        transformer.withParallaxView(R.id.page_fragment_imageHolderFront, 1.2f);
        setPageTransformer(false, transformer);
    }

    /**
     * Sets this IntroActivity to use a ColorBlender background manager.
     */
    private void configureBackground() {
        final BackgroundManager backgroundManager = new ColorBlender(BACKGROUND_COLORS);
        setBackgroundManager(backgroundManager);
    }
}