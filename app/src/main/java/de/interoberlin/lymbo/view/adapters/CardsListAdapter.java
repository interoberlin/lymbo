package de.interoberlin.lymbo.view.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.CardsController;
import de.interoberlin.lymbo.model.Displayable;
import de.interoberlin.lymbo.model.card.Card;
import de.interoberlin.lymbo.model.card.Side;
import de.interoberlin.lymbo.model.card.Tag;
import de.interoberlin.lymbo.model.card.components.Answer;
import de.interoberlin.lymbo.model.card.components.ChoiceComponent;
import de.interoberlin.lymbo.model.card.components.ResultComponent;
import de.interoberlin.lymbo.model.card.components.TextComponent;
import de.interoberlin.lymbo.model.card.components.TitleComponent;
import de.interoberlin.lymbo.model.card.enums.EComponent;
import de.interoberlin.lymbo.util.ViewUtil;
import de.interoberlin.lymbo.view.activities.CardsActivity;
import de.interoberlin.lymbo.view.dialogfragments.CardDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.CopyCardDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.DisplayHintDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.EditNoteDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.MoveCardDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.SelectTagsDialogFragment;

public class CardsListAdapter extends ArrayAdapter<Card> implements Filterable {
    // Context
    private Context context;
    private Activity activity;

    // Controllers
    private CardsController cardsController;

    // Filter
    private List<Card> filteredItems = new ArrayList<>();
    private List<Card> originalItems = new ArrayList<>();
    private CardListFilter cardListFilter;
    private final Object lock = new Object();

    // Properties
    private static int VIBRATION_DURATION;

    // --------------------
    // Constructors
    // --------------------

    public CardsListAdapter(Context context, Activity activity, int resource, List<Card> items) {
        super(context, resource, items);
        cardsController = CardsController.getInstance(activity);

        this.filteredItems = items;
        this.originalItems = items;

        this.context = context;
        this.activity = activity;

        // Properties
        VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);

        filter();
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public int getCount() {
        return filteredItems != null ? filteredItems.size() : 0;
    }

    @Override
    public Card getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final Card card = getItem(position);
        return getCardView(position, card, parent);
    }

    private View getCardView(final int position, final Card card, final ViewGroup parent) {
        // Layout inflater
        LayoutInflater vi;
        vi = LayoutInflater.from(getContext());
        final LinearLayout llCard = (LinearLayout) vi.inflate(R.layout.card, parent, false);

        // Load views : components
        final RelativeLayout rlMain = (RelativeLayout) llCard.findViewById(R.id.rlMain);

        // Load views : bottom bar
        final LinearLayout llTags = (LinearLayout) llCard.findViewById(R.id.llTags);
        final LinearLayout llFlip = (LinearLayout) llCard.findViewById(R.id.llFlip);
        final TextView tvNumerator = (TextView) llCard.findViewById(R.id.tvNumerator);
        final TextView tvDenominator = (TextView) llCard.findViewById(R.id.tvDenominator);
        final ImageView ivNote = (ImageView) llCard.findViewById(R.id.ivNote);
        final ImageView ivFavorite = (ImageView) llCard.findViewById(R.id.ivFavorite);
        final ImageView ivHint = (ImageView) llCard.findViewById(R.id.ivHint);

        final LinearLayout llNoteBar = (LinearLayout) llCard.findViewById(R.id.llNoteBar);
        final TextView tvNote = (TextView) llCard.findViewById(R.id.tvNote);

        // Context menu
        llCard.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
                contextMenu.add(0, 0, 0, getResources().getString(R.string.edit))
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                edit(card);
                                return false;
                            }
                        });
                contextMenu.add(0, 1, 0, getResources().getString(R.string.stash_card))
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                stash(position, card, llCard);
                                return false;
                            }
                        });

                contextMenu.add(0, 2, 0, getResources().getString(R.string.copy_card))
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                copy(card);
                                return false;
                            }
                        });

                contextMenu.add(0, 3, 0, getResources().getString(R.string.move_card))
                        .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                move(card);
                                return false;
                            }
                        });
            }
        });

        // Add sides
        for (Side side : card.getSides()) {
            LayoutInflater li = LayoutInflater.from(context);
            LinearLayout llSide = (LinearLayout) li.inflate(R.layout.side, parent, false);
            LinearLayout llComponents = (LinearLayout) llSide.findViewById(R.id.llComponents);

            // Add components
            for (Displayable d : side.getComponents()) {
                View component = d.getView(context, activity, llComponents);
                llComponents.addView(component);
            }

            llSide.setVisibility(View.INVISIBLE);
            rlMain.addView(llSide);
        }

        // Display width
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final int displayWidth = displaymetrics.widthPixels;

        // Set one side visible
        rlMain.getChildAt(card.getSideVisible()).setVisibility(View.VISIBLE);

        if (!card.isNoteExpanded())
            llNoteBar.getLayoutParams().height = 0;
        if (card.isFavorite())
            ivFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important));
        else
            ivFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_important));

        String note = cardsController.getNote(context, card.getId());

        // Note
        if (note != null && !note.isEmpty()) {
            tvNote.setText(note);
        }

        // Tags
        for (Tag tag : card.getTags()) {
            if (!tag.getName().equals(getResources().getString(R.string.no_tag))) {
                CardView cvTag = (CardView) tag.getView(context, activity, llTags);
                cvTag.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        vibrate(VIBRATION_DURATION);
                        new SelectTagsDialogFragment().show(activity.getFragmentManager(), "okay");
                    }
                });

                llTags.addView(cvTag);
            }
        }

        // Action : flip
        if (card.getSides().size() > 1) {
            tvNumerator.setText(String.valueOf(card.getSideVisible() + 1));
            tvDenominator.setText(String.valueOf(card.getSides().size()));
        } else {
            ViewUtil.remove(llFlip);
        }

        // Action : note
        ivNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (card.isNoteExpanded()) {
                    Animation anim = ViewUtil.collapse(context, llNoteBar);
                    llNoteBar.startAnimation(anim);
                    card.setNoteExpanded(false);

                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ivNote.setImageResource(R.drawable.ic_action_expand);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                } else {
                    Animation anim = ViewUtil.expand(context, llNoteBar);
                    llNoteBar.startAnimation(anim);
                    card.setNoteExpanded(true);

                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            ivNote.setImageResource(R.drawable.ic_action_collapse);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
            }
        });

        // Action : edit note
        tvNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditNoteDialogFragment editNoteDialogFragment = new EditNoteDialogFragment();
                Bundle b = new Bundle();
                b.putCharSequence("uuid", card.getId());
                b.putCharSequence("note", tvNote.getText());

                editNoteDialogFragment.setArguments(b);
                editNoteDialogFragment.show(activity.getFragmentManager(), "okay");
            }
        });

        // Action : favorite
        ivFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(card, !card.isFavorite());
            }
        });


        // Action : hint
        if (card.getHint() != null) {
            ivHint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DisplayHintDialogFragment displayHintDialogFragment = new DisplayHintDialogFragment();
                    Bundle b = new Bundle();
                    b.putCharSequence("title", getResources().getString(R.string.hint));
                    b.putCharSequence("message", card.getHint());

                    displayHintDialogFragment.setArguments(b);
                    displayHintDialogFragment.show(activity.getFragmentManager(), "okay");
                }
            });
        } else {
            ViewUtil.remove(ivHint);
        }

        // Restoring animation
        if (card.isRestoring()) {
            llCard.setTranslationX(displayWidth);

            Animation anim = ViewUtil.expand(context, llCard);
            llCard.startAnimation(anim);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    card.setRestoring(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Animation anim = ViewUtil.fromRight(context, llCard, displayWidth);
                    llCard.startAnimation(anim);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }

        return llCard;
    }

    /**
     * Opens dialog to edit card
     *
     * @param card   card to be edited
     */
    private void edit(final Card card) {
        String uuid = card.getId();
        String frontTitle = ((TitleComponent) card.getSides().get(0).getFirst(EComponent.TITLE)).getValue();
        String backTitle = ((TitleComponent) card.getSides().get(1).getFirst(EComponent.TITLE)).getValue();
        ArrayList<String> frontTexts = new ArrayList<>();
        ArrayList<String> backTexts = new ArrayList<>();
        ArrayList<String> tagsLymbo = new ArrayList<>();
        ArrayList<String> tagsCard = new ArrayList<>();

        for (Displayable d : card.getSides().get(0).getComponents()) {
            if (d instanceof TextComponent) {
                frontTexts.add(((TextComponent) d).getValue());
            }
        }

        for (Displayable d : card.getSides().get(1).getComponents()) {
            if (d instanceof TextComponent) {
                backTexts.add(((TextComponent) d).getValue());
            }
        }

        for (Tag tag : cardsController.getLymbo().getTags()) {
            tagsLymbo.add(tag.getName());
        }

        for (Tag tag : card.getTags()) {
            tagsCard.add(tag.getName());
        }

        vibrate(VIBRATION_DURATION);
        CardDialogFragment dialog = new CardDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.bundle_card_uuid), uuid);
        bundle.putString(getResources().getString(R.string.bundle_front_title), frontTitle);
        bundle.putString(getResources().getString(R.string.bundle_back_title), backTitle);
        bundle.putStringArrayList(getResources().getString(R.string.bundle_texts_front), frontTexts);
        bundle.putStringArrayList(getResources().getString(R.string.bundle_texts_back), backTexts);
        bundle.putStringArrayList(getResources().getString(R.string.bundle_tags_lymbo), tagsLymbo);
        bundle.putStringArrayList(getResources().getString(R.string.bundle_tags_card), tagsCard);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "okay");
    }

    /**
     * Opens dialog to stash card
     *
     * @param position position of item
     * @param card     card to be stashed
     * @param llCard   corresponding view
     */
    private void stash(final int position, final Card card, final LinearLayout llCard) {
        Animation a = ViewUtil.collapse(context, llCard);
        llCard.startAnimation(a);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                stash(position, card);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * Stashes a card
     *
     * @param pos  position of item
     * @param card card
     */
    private void stash(int pos, Card card) {
        cardsController.stash(card);
        ((CardsActivity) activity).stash(pos, card);
        filter();
    }

    /**
     * Opens dialog to copy card
     *
     * @param card card to be copied
     */
    private void copy(final Card card) {
        String uuid = card.getId();

        CopyCardDialogFragment dialog = new CopyCardDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.bundle_lymbo_uuid), cardsController.getLymbo().getId());
        bundle.putString(getResources().getString(R.string.bundle_card_uuid), uuid);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "okay");
    }

    /**
     * Opens dialog to move card
     *
     * @param card card to be moved
     */
    private void move(final Card card) {
        String uuid = card.getId();

        MoveCardDialogFragment dialog = new MoveCardDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.bundle_lymbo_uuid), cardsController.getLymbo().getId());
        bundle.putString(getResources().getString(R.string.bundle_card_uuid), uuid);
        dialog.setArguments(bundle);
        dialog.show(activity.getFragmentManager(), "okay");
    }


    /**
     * Flips a card to the next side
     *
     * @param card card to be flipped
     * @param view corresponding view
     */
    public void flip(final Card card, final View view) {
        if (!checkAnswerSelected(card))
            return;

        final int CARD_FLIP_TIME = getResources().getInteger(R.integer.card_flip_time);
        final int VIBRATION_DURATION_FLIP = getResources().getInteger(R.integer.vibration_duration_flip);

        ObjectAnimator animation = ObjectAnimator.ofFloat(view, "rotationY", 0.0f, 90.0f);
        animation.setDuration(CARD_FLIP_TIME / 2);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();

        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeSide(card, view);

                ObjectAnimator animation = ObjectAnimator.ofFloat(view, "rotationY", -90.0f, 0.0f);
                animation.setDuration(CARD_FLIP_TIME / 2);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.start();
            }
        }, CARD_FLIP_TIME / 2);

        vibrate(VIBRATION_DURATION_FLIP);
    }


    /**
     * Toggles the favorite state of an item
     *
     * @param card card
     */
    private void toggleFavorite(Card card, boolean favorite) {
        cardsController.toggleFavorite(context, card, favorite);
        ((CardsActivity) activity).toggleFavorite(favorite);
        filter();
    }

    /**
     * Displays next side
     *
     * @param card card
     * @param view view of card
     */
    private void changeSide(Card card, View view) {
        final RelativeLayout rlMain = (RelativeLayout) view.findViewById(R.id.rlMain);
        final TextView tvNumerator = (TextView) view.findViewById(R.id.tvNumerator);

        handleQuiz(card);
        card.setSideVisible((card.getSideVisible() + 1) % card.getSides().size());
        tvNumerator.setText(String.valueOf(card.getSideVisible() + 1));

        for (View v : getAllChildren(rlMain)) {
            v.setVisibility(View.INVISIBLE);
        }

        rlMain.getChildAt(card.getSideVisible()).setVisibility(View.VISIBLE);
    }

    /**
     * Returns all direct children of a view
     *
     * @param v view to get children from
     * @return list of all child views
     */
    private List<View> getAllChildren(View v) {
        List<View> children = new ArrayList<>();

        if (!(v instanceof ViewGroup)) {
            return children;
        } else {
            ViewGroup viewGroup = (ViewGroup) v;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                children.add(viewGroup.getChildAt(i));
            }
            return children;
        }
    }

    /**
     * Determines whether at least one answer is selected
     *
     * @param card card
     * @return whether at least one answer is selected
     */
    private boolean checkAnswerSelected(Card card) {
        if (card.getSides().get(card.getSideVisible()).contains(EComponent.CHOICE)) {
            for (Answer a : ((ChoiceComponent) card.getSides().get(0).getFirst(EComponent.CHOICE)).getAnswers()) {
                if (a.isSelected()) {
                    return true;
                }
            }

            ((CardsActivity) activity).alertSelectAnswer();

            return false;
        } else {
            return true;
        }
    }

    /**
     * Determines if all the right answers are selected
     *
     * @param card card
     */
    private void handleQuiz(Card card) {
        Side current = card.getSides().get(card.getSideVisible());

        Side next = null;
        if (card.getSideVisible() + 1 < card.getSides().size())
            next = card.getSides().get(card.getSideVisible() + 1);

        // Handle quiz card
        if (current != null && current.contains(EComponent.CHOICE) && next != null && next.contains(EComponent.RESULT)) {
            // Default result : CORRECT
            ((ResultComponent) next.getFirst(EComponent.RESULT)).setValue(getResources().getString(R.string.correct).toUpperCase(Locale.getDefault()));
            ((ResultComponent) next.getFirst(EComponent.RESULT)).setColor(getResources().getColor(R.color.correct));

            for (Answer a : ((ChoiceComponent) current.getFirst(EComponent.CHOICE)).getAnswers()) {
                if (a.isCorrect() != a.isSelected()) {
                    // At least on answer is wrong : WRONG
                    ((ResultComponent) next.getFirst(EComponent.RESULT)).setValue(getResources().getString(R.string.wrong).toUpperCase(Locale.getDefault()));
                    ((ResultComponent) next.getFirst(EComponent.RESULT)).setColor(getResources().getColor(R.color.wrong));
                    break;
                }
            }

            notifyDataSetChanged();
        }
    }

    public List<Card> getFilteredItems() {
        return filteredItems;
    }

    public void filter() {
        getFilter().filter("");
    }

    @Override
    public Filter getFilter() {
        if (cardListFilter == null) {
            cardListFilter = new CardListFilter();
        }
        return cardListFilter;
    }

    /**
     * Determines if a card shall be displayed
     *
     * @param card card
     * @return true if item is visible
     */
    protected boolean filterCard(Card card) {
        return cardsController.isVisible(card);
    }

    // --------------------
    // Methods - Util
    // --------------------

    private void vibrate (int vibrationDuration) {
        ((Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(vibrationDuration);
    }

    private Resources getResources() {
        return activity.getResources();
    }

    // --------------------
    // Inner classes
    // --------------------

    public class CardListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            // Copy items
            originalItems = cardsController.getCards();

            ArrayList<Card> values;
            synchronized (lock) {
                values = new ArrayList<>(originalItems);
            }

            final int count = values.size();
            final ArrayList<Card> newValues = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                final Card value = values.get(i);
                if (filterCard(value)) {
                    newValues.add(value);
                }
            }

            results.values = newValues;
            results.count = newValues.size();

            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<Card>) results.values;

            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}