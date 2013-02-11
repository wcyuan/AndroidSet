package com.conanyuan.androidset;

import java.util.ArrayList;
import java.util.Random;

import android.os.Bundle;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
    /* Every card has 4 attributes */
    private static final int   N_ATTRS      = 4;

    /* Ever attribute has 3 possible values */
    private static final int   N_VALUES     = 3;

    /* The number of cards shown initially */
    private static final int   N_INIT_CARDS = 12;

    /* The number of cards to deal at a time */
    private static final int   N_AT_A_TIME  = 3;

    /* The number of cards to deal at a time */
    private static final int   NUM_CARDS    = (int) Math.pow(N_VALUES, N_ATTRS);

    /*
     * Cards are just integers whose value is:
     * "shading * 27 + shape * 9 + color * 3 + number + 1".gif where each
     * attribute takes a value from 0 to 2
     */

    private Deck               mDeck;
    private ViewPager          mPager;
    private MyPagerAdapter     mAdapter;

    /**
     * mShownCard is an array of the cards being shown. The index into the array
     * is the position in the grid. The value at that index is the card being
     * show in that spot in the grid.
     */
    private ArrayList<Integer> mShownCards  = new ArrayList<Integer>();

    /**
     * mSelected is an array of the cards which are currently selected. The
     * index is which card it is, and the value is whether or not the card is
     * selected.
     */
    private boolean[]          mSelected;

    /**
     * mFound is an array of the cards which make up the previously found sets.
     * The index is the order in which the cards were found, and the values are
     * the cards that were found.
     */
    private ArrayList<Integer> mFound       = new ArrayList<Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_layout);
        mSelected = new boolean[NUM_CARDS];
        mAdapter = new MyPagerAdapter(getSupportFragmentManager(), mShownCards,
                mSelected, mFound);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mDeck = new Deck(NUM_CARDS);
        newGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    /*
     * (non-Javadoc) ********************************************************
     * Begin gameplay logic
     */

    public void addCard(int card) {
        mShownCards.add(card);
    }

    public void removeCard(int card) {
        for (int ii = 0; ii < mShownCards.size(); ii++) {
            if (mShownCards.get(ii) == card) {
                mShownCards.remove(ii);
            }
        }
    }

    public void replaceCard(int card, int newcard) {
        for (int ii = 0; ii < mShownCards.size(); ii++) {
            if (mShownCards.get(ii) == card) {
                mShownCards.set(ii, newcard);
            }
        }
    }

    /**
     * Given N_VALUES cards, see if it is a set. To be a set, for all
     * attributes, all the values have to be the same or all have to be
     * different.
     * 
     * @param cards
     * @return
     */
    public static boolean isSet(int[] cards) {
        if (cards.length != N_VALUES) {
            return false;
        }
        for (int ii = 0, mask = 1; ii < N_ATTRS; ii++, mask *= 3) {
            boolean allsame = true;
            boolean alldifferent = true;
            int[] vals = new int[N_VALUES];
            int[] possibles = new int[N_VALUES];
            for (int jj = 0; jj < N_VALUES; jj++) {
                vals[jj] = (cards[jj] % (mask * N_VALUES)) / mask;
                if (possibles[vals[jj]] != 0) {
                    alldifferent = false;
                }
                possibles[vals[jj]] = 1;
                if (jj > 0 && vals[jj] != vals[0]) {
                    allsame = false;
                }
            }
            if (!allsame && !alldifferent) {
                return false;
            }
        }
        return true;
    }

    /**
     * If we don't know the selected cards, but we know the positions of those
     * cards, translate the positions in to cards and test those.
     * 
     * @param positions
     * @return
     */
    private boolean isSetPositions(int[] positions) {
        if (positions.length != N_VALUES) {
            return false;
        }
        int[] cards = new int[N_VALUES];
        for (int ii = 0; ii < N_VALUES; ii++) {
            cards[ii] = mShownCards.get(positions[ii]);
        }
        return isSet(cards);
    }

    public void newGame() {
        mDeck.shuffle();
        for (int ii = 0; ii < mSelected.length; ii++) {
            mSelected[ii] = false;
        }
        mFound.clear();
        mShownCards.clear();
        deal();
        mAdapter.refreshShown();
        mAdapter.refreshFound();
    }

    /**
     * Check to see if a set exists among all the cards showing.
     * @return
     */
    public boolean setExists() {
        if (mShownCards.size() < N_VALUES) {
            return false;
        }

        // Loop over all sets of N_VALUES cards.
        // First start with the cards in positions {0, 1, 2},
        // then {0, 1, 3}, {0, 1, 4}, ... {0, 1, 11}, {0, 2, 3},
        // {0, 2, 4}, etc.
        int[] possSet = new int[N_VALUES];
        for (int ii = 0; ii < N_VALUES; ii++) {
            possSet[ii] = ii;
        }
        while (true) {
            if (isSetPositions(possSet)) {
                return true;
            }
            // If it isn't a set, we've got to increment to the next possible
            // set.
            for (int ii = N_VALUES - 1; ii >= 0; ii--) {
                possSet[ii]++;
                // In most cases, we can just increment the least significant
                // bit. But once the least significant bit is as high as it can
                // go, we've got to continue to the next bit and increment that
                // one.
                //
                // The highest each bit can go is only high enough that all the
                // bits after it can still be less than the number of cards
                // shown.
                if (possSet[ii] < mShownCards.size() - N_VALUES + ii + 1) {
                    // After we've set this bit, we have to set all the
                    // bits after it.
                    for (int jj = ii + 1; jj < N_VALUES; jj++) {
                        possSet[jj] = possSet[jj - 1] + 1;
                    }
                    break;
                }
                // If this is the most significant bit, then we've tried
                // everything.
                if (ii == 0) {
                    return false;
                }
            }
        }
    }

    public void deal() {
        if (mDeck.endOfDeck() && !setExists()) {
            Toast.makeText(this, "End of Deck!", Toast.LENGTH_SHORT).show();
            return;
        }
        while (!mDeck.endOfDeck()
                && (!setExists() || mShownCards.size() < N_INIT_CARDS))
        {
            for (int ii = 0; ii < N_AT_A_TIME; ii++) {
                addCard(mDeck.nextCard());
            }
        }
        mAdapter.refreshShown();
    }

    /**
     * Check to see if the cards that the user selected make up a set.
     */
    public void checkSet() {
        int[] selected = new int[N_VALUES];
        int[] selpos = new int[N_VALUES];
        int num_selected = 0;
        for (int ii = 0; ii < mShownCards.size(); ii++) {
            if (mSelected[mShownCards.get(ii)]) {
                selected[num_selected] = mShownCards.get(ii);
                selpos[num_selected] = ii;
                num_selected++;
                if (num_selected >= N_VALUES) {
                    break;
                }
            }
        }
        if (num_selected < N_VALUES) {
            return;
        }

        if (isSet(selected)) {
            Toast.makeText(this, "Found a set!", Toast.LENGTH_SHORT).show();
            for (int pos: selpos) {
                mFound.add(mShownCards.get(pos));
            }
            mAdapter.refreshFound();
            if (mShownCards.size() <= N_INIT_CARDS && !mDeck.endOfDeck()) {
                for (int pos: selpos) {
                    mShownCards.set(pos, mDeck.nextCard());
                }
            } else {
                int num_shown = mShownCards.size();
                int ii = 0;
                for (int pos = num_shown-1; pos > num_shown-4; pos--) {
                    if (mSelected[mShownCards.get(pos)]) {
                        mShownCards.remove(pos);
                    } else {
                        mShownCards.set(selpos[ii], mShownCards.get(pos));
                        mShownCards.remove(pos);
                        ii++;
                    }
                }
            }
        } else {
            Toast.makeText(this, "Not a set", Toast.LENGTH_SHORT).show();
        }
        for (int card : selected) {
            mSelected[card] = false;
        }
        deal();
        mAdapter.refreshShown();
    }

    /*
     * End gameplay logic. Everything below here is dealing with inner classes.
     * *******************************************************
     */

    /**
     * Deck class
     * 
     * @author Yuan
     */
    public static class Deck {
        private int[]  mCards;
        private Random mRand;
        private int    mCurrent;

        public Deck(int nCards) {
            mCards = new int[nCards];
            for (int i = 0; i < nCards; i++) {
                mCards[i] = i;
            }
            mRand = new Random();
            shuffle();
        }

        private void shuffle() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < mCards.length; j++) {
                    int k = Math.abs(mRand.nextInt() % mCards.length);
                    /* swap */
                    int temp = mCards[j];
                    mCards[j] = mCards[k];
                    mCards[k] = temp;
                }
            }
            mCurrent = 0;
        }

        public int nextCard() {
            if (endOfDeck()) {
                shuffle();
            }
            return mCards[mCurrent++];
        }

        public boolean endOfDeck() {
            return mCurrent >= mCards.length;
        }
    }

    /**
     * Pager Adapter
     * 
     * @author Yuan
     */
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private GameFragment       mGameFragment;
        private FoundFragment      mFoundFragment;
        private ArrayList<Integer> mShownCards;
        private boolean[]          mSelected;
        private ArrayList<Integer> mFound;

        public MyPagerAdapter(FragmentManager fragmentManager,
                ArrayList<Integer> shown, boolean[] selected,
                ArrayList<Integer> found)
        {
            super(fragmentManager);
            mShownCards = shown;
            mSelected = selected;
            mFound = found;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                mGameFragment = GameFragment
                        .newInstance(mShownCards, mSelected);
                return mGameFragment;
            case 1:
                mFoundFragment = FoundFragment.newInstance(mFound);
                return mFoundFragment;
            default:
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return "Play Game";
            case 1:
                return "Previous Sets";
            default:
                return "Unknown";
            }
        }

        public void refreshShown() {
            if (mGameFragment != null) {
                mGameFragment.refreshView();
            }
        }

        public void refreshFound() {
            if (mFoundFragment != null) {
                mFoundFragment.refreshView();
            }
        }
    }

    /**
     * GameFragment
     * 
     * @author Yuan
     */
    public static class GameFragment extends Fragment {
        private ImageAdapter       mAdapter;
        private ArrayList<Integer> mShownCards;
        private boolean[]          mSelected;

        /**
         * Create a new instance of GameFragment
         */
        static GameFragment newInstance(ArrayList<Integer> shown,
                boolean[] selected)
        {
            GameFragment fragment = new GameFragment();

            Bundle args = new Bundle();
            args.putIntegerArrayList("shown", shown);
            args.putBooleanArray("selected", selected);
            fragment.setArguments(args);
            return fragment;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mShownCards = getArguments().getIntegerArrayList("shown");
                mSelected = getArguments().getBooleanArray("selected");
            }
        }

        /**
         * The Fragment's UI is just a simple text view showing its instance
         * number.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View v = inflater.inflate(R.layout.activity_main, container, false);
            GridView grid = (GridView) v.findViewById(R.id.card_grid);
            mAdapter = new ImageAdapter(getActivity(), mShownCards, mSelected);
            grid.setAdapter(mAdapter);

            grid.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View v,
                        int position, long id)
                {
                    mAdapter.toggleSelected(position);
                    ((MainActivity) getActivity()).checkSet();
                    mAdapter.notifyDataSetChanged();
                }
            });

            Button new_game = (Button) v.findViewById(R.id.new_game);
            new_game.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).newGame();
                }
            });
            return v;
        }

        public void refreshView() {
            mAdapter.notifyDataSetChanged();
        }

        public ImageAdapter getAdapter() {
            return mAdapter;
        }
    }

    /**
     * ImageAdapter manages the grid used by both the Game and Found fragments
     * 
     * @author Yuan
     */
    public static class ImageAdapter extends BaseAdapter {
        private Context            mContext;
        private ArrayList<Integer> mShownCards;
        private boolean[]          mSelected;

        public ImageAdapter(Context c, ArrayList<Integer> shown,
                boolean[] selected)
        {
            mContext = c;
            mShownCards = shown;
            mSelected = selected;
        }

        @Override
        public int getCount() {
            return mShownCards.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, initialize some
                                       // attributes
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                imageView = (ImageView) convertView;
            }

            if (isSelected(position)) {
                imageView.setBackgroundColor(Color.YELLOW);
            } else {
                imageView.setBackgroundColor(Color.BLACK);
            }
            imageView.setImageResource(mImageIds[mShownCards.get(position)]);
            return imageView;
        }

        private boolean isSelected(int position) {
            if (mSelected == null) {
                return false;
            }
            if (position >= mShownCards.size()) {
                return false;
            }
            if (mShownCards.get(position) >= mSelected.length) {
                return false;
            }
            return mSelected[mShownCards.get(position)];
        }

        public void toggleSelected(int position) {
            if (mSelected == null) {
                return;
            }
            if (position >= mShownCards.size()) {
                return;
            }
            int card = mShownCards.get(position);
            if (card >= mSelected.length) {
                return;
            }
            mSelected[card] = !mSelected[card];
        }
    }

    /**
     * FoundFragment
     * 
     * @author Yuan
     */
    public static class FoundFragment extends Fragment {
        private ImageAdapter       mAdapter;
        private ArrayList<Integer> mFound = new ArrayList<Integer>();

        /**
         * Create a new instance of FoundFragment
         */
        static FoundFragment newInstance(ArrayList<Integer> found) {
            FoundFragment fragment = new FoundFragment();

            Bundle args = new Bundle();
            args.putIntegerArrayList("found", found);
            fragment.setArguments(args);
            return fragment;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mFound = getArguments().getIntegerArrayList("found");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            View v = inflater.inflate(R.layout.activity_main, container, false);
            // XXX should have a different view...
            GridView grid = (GridView) v.findViewById(R.id.card_grid);
            mAdapter = new ImageAdapter(getActivity(), mFound, null);
            grid.setAdapter(mAdapter);
            return v;
        }

        public void refreshView() {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * mImageIds contains references to our images
     * 
     * image name = "shading * 27 + shape * 9 + color * 3 + number + 1".gif for
     * zero indexed variables.
     */
    private static Integer[] mImageIds = { R.drawable.card_1,
            R.drawable.card_2, R.drawable.card_3, R.drawable.card_4,
            R.drawable.card_5, R.drawable.card_6, R.drawable.card_7,
            R.drawable.card_8, R.drawable.card_9, R.drawable.card_10,
            R.drawable.card_11, R.drawable.card_12, R.drawable.card_13,
            R.drawable.card_14, R.drawable.card_15, R.drawable.card_16,
            R.drawable.card_17, R.drawable.card_18, R.drawable.card_19,
            R.drawable.card_20, R.drawable.card_21, R.drawable.card_22,
            R.drawable.card_23, R.drawable.card_24, R.drawable.card_25,
            R.drawable.card_26, R.drawable.card_27, R.drawable.card_28,
            R.drawable.card_29, R.drawable.card_30, R.drawable.card_31,
            R.drawable.card_32, R.drawable.card_33, R.drawable.card_34,
            R.drawable.card_35, R.drawable.card_36, R.drawable.card_37,
            R.drawable.card_38, R.drawable.card_39, R.drawable.card_40,
            R.drawable.card_41, R.drawable.card_42, R.drawable.card_43,
            R.drawable.card_44, R.drawable.card_45, R.drawable.card_46,
            R.drawable.card_47, R.drawable.card_48, R.drawable.card_49,
            R.drawable.card_50, R.drawable.card_51, R.drawable.card_52,
            R.drawable.card_53, R.drawable.card_54, R.drawable.card_55,
            R.drawable.card_56, R.drawable.card_57, R.drawable.card_58,
            R.drawable.card_59, R.drawable.card_60, R.drawable.card_61,
            R.drawable.card_62, R.drawable.card_63, R.drawable.card_64,
            R.drawable.card_65, R.drawable.card_66, R.drawable.card_67,
            R.drawable.card_68, R.drawable.card_69, R.drawable.card_70,
            R.drawable.card_71, R.drawable.card_72, R.drawable.card_73,
            R.drawable.card_74, R.drawable.card_75, R.drawable.card_76,
            R.drawable.card_77, R.drawable.card_78, R.drawable.card_79,
            R.drawable.card_80, R.drawable.card_81, };
}
