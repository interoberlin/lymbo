package de.interoberlin.lymbo.view.adapters;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.CardsController;
import de.interoberlin.lymbo.model.Displayable;
import de.interoberlin.lymbo.model.card.Card;
import de.interoberlin.lymbo.model.card.Side;
import de.interoberlin.lymbo.model.card.Tag;
import de.interoberlin.lymbo.model.card.components.Answer;
import de.interoberlin.lymbo.model.card.components.ChoiceComponent;
import de.interoberlin.lymbo.model.card.components.ImageComponent;
import de.interoberlin.lymbo.model.card.components.ResultComponent;
import de.interoberlin.lymbo.model.card.components.SVGComponent;
import de.interoberlin.lymbo.model.card.components.TextComponent;
import de.interoberlin.lymbo.model.card.components.TitleComponent;
import de.interoberlin.lymbo.model.card.enums.EComponent;
import de.interoberlin.lymbo.util.Configuration;
import de.interoberlin.lymbo.util.EProperty;
import de.interoberlin.lymbo.util.ViewUtil;
import de.interoberlin.lymbo.view.activities.CardsActivity;
import de.interoberlin.lymbo.view.dialogfragments.DisplayHintDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.EditNoteDialogFragment;
import de.interoberlin.lymbo.view.dialogfragments.SelectTagsDialogFragment;

public class CardsListAdapter extends ArrayAdapter<Card> {
    // Context
    private Context c;
    private Activity a;

    // Controllers
    CardsController cardsController;

    // Properties
    private static int VIBRATION_DURATION;

    // --------------------
    // Constructors
    // --------------------

    public CardsListAdapter(Context context, Activity activity, int resource, List<Card> items) {
        super(context, resource, items);
        cardsController = CardsController.getInstance(activity);

        this.c = context;
        this.a = activity;

        // Properties
        VIBRATION_DURATION = Integer.parseInt(Configuration.getProperty(c, EProperty.VIBRATION_DURATION));
    }

    // --------------------
    // Methods
    // --------------------

    @Override
    public View getView(final int position, View v, ViewGroup parent) {
        final Card card = getItem(position);

        return getCardView(position, card, parent);
    }

    private View getCardView(final int position, final Card card, final ViewGroup parent) {
        if (card != null) {
            if (!card.isDiscarded() && card.matchesChapter(cardsController.getLymbo().getChapters()) && card.matchesTag(cardsController.getLymbo().getTags())) {
                // Layout inflater
                LayoutInflater vi;
                vi = LayoutInflater.from(getContext());
                final FrameLayout flCard = (FrameLayout) vi.inflate(R.layout.card, parent, false);

                // Load views : components
                final RelativeLayout rlMain = (RelativeLayout) flCard.findViewById(R.id.rlMain);

                // Load views : bottom bar
                final LinearLayout llTags = (LinearLayout) flCard.findViewById(R.id.llTags);
                final View divider = flCard.findViewById(R.id.divider);
                final LinearLayout llIconbar = (LinearLayout) flCard.findViewById(R.id.llIconbar);
                final LinearLayout llFlip = (LinearLayout) flCard.findViewById(R.id.llFlip);
                final TextView tvNumerator = (TextView) flCard.findViewById(R.id.tvNumerator);
                final TextView tvDenominator = (TextView) flCard.findViewById(R.id.tvDenominator);
                final ImageView ivNote = (ImageView) flCard.findViewById(R.id.ivNote);
                final ImageView ivEdit = (ImageView) flCard.findViewById(R.id.ivEdit);
                final ImageView ivHint = (ImageView) flCard.findViewById(R.id.ivHint);

                final LinearLayout llNoteBar = (LinearLayout) flCard.findViewById(R.id.llNoteBar);
                final TextView tvNote = (TextView) flCard.findViewById(R.id.tvNote);
                final ImageView ivEditNote = (ImageView) flCard.findViewById(R.id.ivEditNote);

                // Load views : reveal
                final ImageView ivStash = (ImageView) flCard.findViewById(R.id.ivStash);
                final ImageView ivDiscard = (ImageView) flCard.findViewById(R.id.ivDiscard);
                final ImageView ivPutToEnd = (ImageView) flCard.findViewById(R.id.ivToEnd);

                // Add sides
                for (Side side : card.getSides()) {
                    LayoutInflater li = LayoutInflater.from(c);
                    LinearLayout llSide = (LinearLayout) li.inflate(R.layout.side, parent, false);
                    LinearLayout llComponents = (LinearLayout) llSide.findViewById(R.id.llComponents);

                    // Add components
                    for (Displayable d : side.getComponents()) {
                        View component = d.getView(c, a, llComponents);
                        llComponents.addView(component);

                        if ((d instanceof TitleComponent && ((TitleComponent) d).isFlip()) ||
                                (d instanceof TextComponent && ((TextComponent) d).isFlip()) ||
                                (d instanceof ImageComponent && ((ImageComponent) d).isFlip()) ||
                                (d instanceof ResultComponent && ((ResultComponent) d).isFlip()) ||
                                (d instanceof SVGComponent && ((SVGComponent) d).isFlip())
                                ) {
                            component.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    flip(card, flCard);
                                }
                            });

                        }
                    }

                    llSide.setVisibility(View.INVISIBLE);

                    rlMain.addView(llSide);
                }

                // Display width
                DisplayMetrics displaymetrics = new DisplayMetrics();
                a.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                final int displayWidth = displaymetrics.widthPixels;

                rlMain.getChildAt(card.getSideVisible()).setVisibility(View.VISIBLE);

                if (!card.isNoteExpanded())
                    llNoteBar.getLayoutParams().height = 0;

                String note = cardsController.getNote(c, card.getId());

                // Note
                if (note != null)
                    tvNote.setText(note);

                // Tags
                for (Tag tag : card.getTags()) {
                    if (!tag.getName().equals(c.getResources().getString(R.string.no_tag))) {
                        CardView cvTag = (CardView) tag.getView(c, a, llTags);
                        cvTag.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ((Vibrator) a.getSystemService(c.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);
                                new SelectTagsDialogFragment().show(a.getFragmentManager(), "okay");
                            }
                        });

                        llTags.addView(cvTag);
                    }
                }

                // Action : flip
                if (card.getSides().size() > 1) {
                    tvNumerator.setText(String.valueOf(card.getSideVisible() + 1));
                    tvDenominator.setText(String.valueOf(card.getSides().size()));

                    llFlip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            flip(card, flCard);
                        }
                    });
                } else {
                    ViewUtil.remove(llFlip);
                }

                // Action : edit
                /*
                if (card.isEdit()) {
                    ivEdit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            componentsController.setCard(card);
                            Intent openStartingPoint = new Intent(c, EditCardActivity.class);
                            c.startActivity(openStartingPoint);
                        }
                    });
                } else {
                */
                ViewUtil.remove(ivEdit);
                //}

                // Action : note
                ivNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.isNoteExpanded()) {
                            Animation anim = ViewUtil.collapse(c, llNoteBar);
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
                            Animation anim = ViewUtil.expand(c, llNoteBar);
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
                ivEditNote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        EditNoteDialogFragment editNoteDialogFragment = new EditNoteDialogFragment();
                        Bundle b = new Bundle();
                        b.putCharSequence("uuid", card.getId());
                        b.putCharSequence("note", tvNote.getText());

                        editNoteDialogFragment.setArguments(b);
                        editNoteDialogFragment.show(a.getFragmentManager(), "okay");
                    }
                });

                // Action : hint
                if (card.getHint() != null) {
                    ivHint.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DisplayHintDialogFragment displayHintDialogFragment = new DisplayHintDialogFragment();
                            Bundle b = new Bundle();
                            b.putCharSequence("title", a.getResources().getString(R.string.hint));
                            b.putCharSequence("message", card.getHint());

                            displayHintDialogFragment.setArguments(b);
                            displayHintDialogFragment.show(a.getFragmentManager(), "okay");
                        }
                    });
                } else {
                    ViewUtil.remove(ivHint);
                }

                // Reveal : stash
                ivStash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.isRevealed()) {
                            Animation a = ViewUtil.collapse(c, flCard);
                            flCard.startAnimation(a);

                            a.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    stash(position, card.getId(), flCard);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                    }
                });

                // Reveal : discard
                ivDiscard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.isRevealed()) {
                            Animation a = ViewUtil.collapse(c, flCard);
                            flCard.startAnimation(a);

                            a.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    discard(position, card.getId(), flCard);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                    }
                });

                // Reveal : put to end
                ivPutToEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (card.isRevealed()) {
                            Animation a = ViewUtil.collapse(c, flCard);
                            flCard.startAnimation(a);

                            a.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    putToEnd(position, card.getId(), flCard);
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                        }
                    }
                });

                if (card.isRestoring()) {
                    flCard.setTranslationX(displayWidth);

                    Animation anim = ViewUtil.expand(c, flCard);
                    flCard.startAnimation(anim);

                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            card.setRestoring(false);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            Animation anim = ViewUtil.fromRight(c, flCard, displayWidth);
                            flCard.startAnimation(anim);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }

                return flCard;
            } else {
                Space s = new Space(c);
                s.setVisibility(View.GONE);
                return s;
            }


        } else {
            // Layout inflater
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            return vi.inflate(R.layout.toolbar_space, parent, false);
        }
    }

    private void flip(final Card card, final FrameLayout flCard) {
        if (!checkAnswerSelected(card))
            return;

        final int CARD_FLIP_TIME = c.getResources().getInteger(R.integer.card_flip_time);
        final int VIBRATION_DURATION_FLIP = c.getResources().getInteger(R.integer.vibration_duration_flip);

        ObjectAnimator animation = ObjectAnimator.ofFloat(flCard, "rotationY", 0.0f, 90.0f);
        animation.setDuration(CARD_FLIP_TIME / 2);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.start();

        flCard.postDelayed(new Runnable() {
            @Override
            public void run() {
                changeSide(card, flCard);

                ObjectAnimator animation = ObjectAnimator.ofFloat(flCard, "rotationY", -90.0f, 0.0f);
                animation.setDuration(CARD_FLIP_TIME / 2);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.start();
            }
        }, CARD_FLIP_TIME / 2);

        ((Vibrator) a.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION_FLIP);
    }

    /**
     * Stashes card
     *
     * @param uuid    position of item
     * @param flCard
     */
    private void stash(int pos, String uuid, FrameLayout flCard) {
        ViewUtil.collapse(c, flCard);

        cardsController.stash(uuid);
        ((CardsActivity) a).stash(pos, uuid);
        notifyDataSetChanged();
    }

    /**
     * Removes an item from the current stack permanently
     *
     * @param uuid    position of item
     * @param flCard
     */
    private void discard(int pos, String uuid, FrameLayout flCard) {
        ViewUtil.collapse(c, flCard);

        cardsController.discard(uuid);
        ((CardsActivity) a).discard(pos, uuid);
        notifyDataSetChanged();
    }

    /**
     * Puts an item to the end of the stack
     *
     * @param uuid position of item
     */
    private void putToEnd(int pos, String uuid, FrameLayout flCard) {
        ViewUtil.collapse(c, flCard);

        cardsController.putToEnd(uuid);
        ((CardsActivity) a).putToEnd(pos, uuid);
        notifyDataSetChanged();
    }

    /**
     * Displays next side
     *
     * @param flCard frameLayout of card
     */
    private void changeSide(Card card, FrameLayout flCard) {
        final RelativeLayout rlMain = (RelativeLayout) flCard.findViewById(R.id.rlMain);
        final TextView tvNumerator = (TextView) flCard.findViewById(R.id.tvNumerator);

        // Handle components
        handleQuiz(card);

        card.setSideVisible(card.getSideVisible() + 1);
        card.setSideVisible(card.getSideVisible() % card.getSides().size());

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

    private boolean checkAnswerSelected(Card card) {
        if (card.getSides().get(card.getSideVisible()).contains(EComponent.CHOICE)) {
            for (Answer a : ((ChoiceComponent) card.getSides().get(0).getFirst(EComponent.CHOICE)).getAnswers()) {
                if (a.isSelected()) {
                    return true;
                }
            }

            ((CardsActivity) a).alertSelectAnswer();

            return false;
        } else {
            return true;
        }
    }

    private void handleQuiz(Card card) {
        Side current = card.getSides().get(card.getSideVisible());

        Side next = null;
        if (card.getSideVisible() + 1 < card.getSides().size())
            next = card.getSides().get(card.getSideVisible() + 1);

        // Handle quiz card
        if (current != null && current.contains(EComponent.CHOICE) && next != null && next.contains(EComponent.RESULT)) {
            // Default result : CORRECT
            ((ResultComponent) next.getFirst(EComponent.RESULT)).setValue(c.getResources().getString(R.string.correct).toUpperCase());
            ((ResultComponent) next.getFirst(EComponent.RESULT)).setColor(c.getResources().getColor(R.color.correct));

            for (Answer a : ((ChoiceComponent) current.getFirst(EComponent.CHOICE)).getAnswers()) {
                if (a.isCorrect() != a.isSelected()) {
                    // At least on answer is wrong : WRONG
                    ((ResultComponent) next.getFirst(EComponent.RESULT)).setValue(c.getResources().getString(R.string.wrong).toUpperCase());
                    ((ResultComponent) next.getFirst(EComponent.RESULT)).setColor(c.getResources().getColor(R.color.wrong));
                    break;
                }
            }

            notifyDataSetChanged();
        }
    }
}