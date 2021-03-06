package de.interoberlin.lymbo.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.CardsController;
import de.interoberlin.lymbo.core.model.v1.impl.Card;
import de.interoberlin.lymbo.core.model.v1.impl.Stack;
import de.interoberlin.lymbo.core.model.v1.impl.Tag;
import de.interoberlin.lymbo.core.model.v1.impl.components.AComponent;
import de.interoberlin.lymbo.core.model.v1.impl.components.Answer;
import de.interoberlin.lymbo.core.model.v1.impl.components.EComponentType;
import de.interoberlin.lymbo.core.model.v1.impl.components.Text;
import de.interoberlin.lymbo.core.model.v1.impl.components.Title;
import de.interoberlin.lymbo.model.webservice.AccessControlItem;
import de.interoberlin.lymbo.model.webservice.translate.MicrosoftAccessControlItemTask;
import de.interoberlin.lymbo.model.webservice.translate.MicrosoftTranslatorTask;
import de.interoberlin.lymbo.util.ViewUtil;

public class CardDialog extends DialogFragment {
    public static final String TAG = CardDialog.class.getSimpleName();

    // Model
    private boolean addTextFrontIsExpanded = false;
    private boolean addQuizIsExpanded = false;
    private boolean addTextBackIsExpanded = false;
    private boolean addTagsIsExpanded = false;
    private boolean useTemplateIsExpanded = false;

    // Properties
    private static int VIBRATION_DURATION;

    private OnCompleteListener ocListener;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CardsController cardsController = CardsController.getInstance();
        final Stack stack = cardsController.getStack();
        final Resources res = getActivity().getResources();

        VIBRATION_DURATION = getResources().getInteger(R.integer.vibration_duration);

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_card, null);

        final EditText etFront = (EditText) v.findViewById(R.id.etFront);
        final ImageView ivTranslateFront = (ImageView) v.findViewById(R.id.ivTranslateFront);
        final ImageView ivExpandTextsFront = (ImageView) v.findViewById(R.id.ivExpandTextsFront);
        final LinearLayout llTextFront = (LinearLayout) v.findViewById(R.id.llTextFront);
        final TableLayout tblTextFront = (TableLayout) v.findViewById(R.id.tblTextFront);
        final ImageView ivAddTextFront = (ImageView) v.findViewById(R.id.ivAddTextFront);
        final LinearLayout llAddQuiz = (LinearLayout) v.findViewById(R.id.llAddQuiz);
        final LinearLayout llQuiz = (LinearLayout) v.findViewById(R.id.llQuiz);
        final TableLayout tblQuiz = (TableLayout) v.findViewById(R.id.tblQuiz);
        final ImageView ivAddAnswer = (ImageView) v.findViewById(R.id.ivAddAnswer);

        final EditText etBack = (EditText) v.findViewById(R.id.etBack);
        final ImageView ivTranslateBack = (ImageView) v.findViewById(R.id.ivTranslateBack);
        final ImageView ivExpandTextsBack = (ImageView) v.findViewById(R.id.ivExpandTextsBack);
        final LinearLayout llTextBack = (LinearLayout) v.findViewById(R.id.llTextBack);
        final TableLayout tblTextBack = (TableLayout) v.findViewById(R.id.tblTextBack);
        final ImageView ivAddTextBack = (ImageView) v.findViewById(R.id.ivAddTextBack);

        final LinearLayout llAddTags = (LinearLayout) v.findViewById(R.id.llAddTags);
        final LinearLayout llTags = (LinearLayout) v.findViewById(R.id.llTags);
        final TableLayout tblTags = (TableLayout) v.findViewById(R.id.tblTags);
        final ImageView ivAddTag = (ImageView) v.findViewById(R.id.ivAddTag);

        final LinearLayout llUseTemplate = (LinearLayout) v.findViewById(R.id.llUseTemplate);
        final LinearLayout llTemplates = (LinearLayout) v.findViewById(R.id.llTemplates);
        final TableLayout tblTemplates = (TableLayout) v.findViewById(R.id.tblTemplates);

        // Tint
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivExpandTextsFront.getDrawable().setTint(ContextCompat.getColor(getActivity(), R.color.card_icon));
            ivExpandTextsBack.getDrawable().setTint(ContextCompat.getColor(getActivity(), R.color.card_icon));
        }

        // Get arguments
        Bundle bundle = this.getArguments();
        final String dialogTitle = bundle.getString(res.getString(R.string.bundle_dialog_title));
        final String frontTitle = bundle.getString(res.getString(R.string.bundle_front_title));
        final String backTitle = bundle.getString(res.getString(R.string.bundle_back_title));
        final ArrayList<String> textsFront = bundle.getStringArrayList(res.getString(R.string.bundle_texts_front));
        final ArrayList<String> textsBack = bundle.getStringArrayList(res.getString(R.string.bundle_texts_back));
        final ArrayList<String> tagsAll = bundle.getStringArrayList(res.getString(R.string.bundle_tags_all));
        final ArrayList<String> tagsSelected = bundle.getStringArrayList(res.getString(R.string.bundle_tags_selected));
        final ArrayList<String> templates = bundle.getStringArrayList(getActivity().getResources().getString(R.string.bundle_templates));
        final ArrayList<String> answersValues = bundle.getStringArrayList(getActivity().getResources().getString(R.string.bundle_answers_value));
        final ArrayList<Integer> answersCorrect = bundle.getIntegerArrayList(getActivity().getResources().getString(R.string.bundle_answers_correct));

        // Sort lists
        if (tagsAll != null)
            Collections.sort(tagsAll);
        if (tagsSelected != null)
            Collections.sort(tagsSelected);

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(dialogTitle);

        if (frontTitle != null)
            etFront.setText(frontTitle);

        if (backTitle != null)
            etBack.setText(backTitle);

        if (textsFront != null) {
            for (String s : textsFront) {
                final TableRow tr = new TableRow(getActivity());
                final EditText etText = new EditText(getActivity());
                tr.addView(etText);
                etText.setText(s);
                tblTextFront.addView(tr, tblTextFront.getChildCount());
            }
        }

        if (textsBack != null) {
            for (String s : textsBack) {
                final TableRow tr = new TableRow(getActivity());
                final EditText etText = new EditText(getActivity());
                tr.addView(etText);
                etText.setText(s);
                tblTextBack.addView(tr, tblTextBack.getChildCount());
            }
        }

        if (answersValues != null && answersCorrect != null && answersValues.size() == answersCorrect.size()) {
            for (int i = 0; i < answersValues.size(); i++) {
                final TableRow tr = new TableRow(getActivity());
                final CheckBox cb = new CheckBox(getActivity());
                final EditText et = new EditText(getActivity());

                tr.addView(cb);
                tr.addView(et);

                cb.setChecked(answersCorrect.get(i) == 1);
                et.setText(answersValues.get(i));
                tblQuiz.addView(tr, tblQuiz.getChildCount());
            }
        }

        if (tagsAll != null) {
            for (final String tag : tagsAll) {
                if (tag != null && !tag.equals(getActivity().getResources().getString(R.string.no_tag))) {
                    final TableRow tr = new TableRow(getActivity());
                    final CheckBox cb = new CheckBox(getActivity());
                    final TextView tvText = new TextView(getActivity());

                    tr.addView(cb);
                    tr.addView(tvText);

                    if (tagsSelected != null && tagsSelected.contains(tag))
                        cb.setChecked(true);

                    tvText.setText(tag);
                    tvText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cb.toggle();
                        }
                    });

                    tblTags.addView(tr, tblTags.getChildCount());
                }
            }
        }

        for (final String t : templates) {
            final TableRow tr = new TableRow(getActivity());
            final TextView tvText = new TextView(getActivity());

            final Card template = cardsController.getTemplateById(t);

            tr.setPadding(0, (int) res.getDimension(R.dimen.table_row_padding), 0, (int) res.getDimension(R.dimen.table_row_padding));
            tvText.setText(template.getTitle());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                tvText.setTextAppearance(android.R.style.TextAppearance_Medium);
            else
                tvText.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
            tvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    useTemplate(template, etFront, etBack, tblTextFront, tblTextBack, tblTags);
                }
            });

            tr.addView(tvText);

            tblTemplates.addView(tr);
        }

        // Add actions
        ivExpandTextsFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandTextsFront(ivExpandTextsFront, llTextFront);
            }
        });

        ivExpandTextsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandTextsBack(ivExpandTextsBack, llTextBack);
            }
        });

        ivTranslateFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String languageFrom = stack.getLanguage().getFrom();
                String languageTo = stack.getLanguage().getTo();
                translate(languageTo, languageFrom, etBack, etFront);
            }
        });

        ivTranslateBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String languageFrom = stack.getLanguage().getFrom();
                String languageTo = stack.getLanguage().getTo();
                translate(languageFrom, languageTo, etFront, etBack);
            }
        });

        llAddQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandQuiz(llQuiz);
            }
        });

        llAddTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandTags(llTags);
            }
        });

        if (templates != null && !templates.isEmpty()) {
            llUseTemplate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    expandTemplates(llTemplates);
                }
            });
        } else {
            ViewUtil.remove(llUseTemplate);
        }

        ivAddTextFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText(tblTextFront);
            }
        });

        ivAddAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAnswer(tblQuiz);
            }
        });

        ivAddTextBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addText(tblTextBack);
            }
        });

        ivAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTag(tblTags);
            }
        });

        llTextFront.getLayoutParams().height = 0;
        llQuiz.getLayoutParams().height = 0;
        llTextBack.getLayoutParams().height = 0;
        llTags.getLayoutParams().height = 0;
        llTemplates.getLayoutParams().height = 0;

        String languageFrom = null;
        String languageTo = null;

        if (stack.getLanguage() != null) {
            languageFrom = stack.getLanguage().getFrom();
            languageTo = stack.getLanguage().getTo();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accessItemAccessToken = prefs.getString(res.getString(R.string.pref_translator_access_item_access_token), null);

        if (languageFrom == null || languageTo == null || accessItemAccessToken == null) {
            ViewUtil.remove(ivTranslateFront);
            ViewUtil.remove(ivTranslateBack);
        }

        // Add positive button
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Add negative button
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get arguments
        Bundle bundle = this.getArguments();
        final String cardUuid = bundle.getString(getActivity().getResources().getString(R.string.bundle_card_uuid));

        AlertDialog dialog = (AlertDialog) getDialog();
        final EditText etFront = (EditText) dialog.findViewById(R.id.etFront);
        final TableLayout tblTextFront = (TableLayout) dialog.findViewById(R.id.tblTextFront);
        final TableLayout tblQuiz = (TableLayout) dialog.findViewById(R.id.tblQuiz);
        final EditText etBack = (EditText) dialog.findViewById(R.id.etBack);
        final TableLayout tblTextBack = (TableLayout) dialog.findViewById(R.id.tblTextBack);
        final TableLayout tblTags = (TableLayout) dialog.findViewById(R.id.tblTags);

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String front = etFront.getText().toString().trim();

                Drawable dWarning = ContextCompat.getDrawable(getActivity(), R.drawable.ic_warning_black_48dp);

                if (front.isEmpty()) {
                    etFront.setError(getActivity().getResources().getString(R.string.field_must_not_be_empty), dWarning);
                } else {
                    if (cardUuid == null) {
                        ocListener.onAddCard(etFront.getText().toString(), getTexts(tblTextFront), etBack.getText().toString(), getTexts(tblTextBack), getSelectedTags(tblTags), getAnswers(tblQuiz));
                    } else {
                        ocListener.onEditCard(cardUuid, etFront.getText().toString(), getTexts(tblTextFront), etBack.getText().toString(), getTexts(tblTextBack), getSelectedTags(tblTags), getAnswers(tblQuiz));
                    }
                    dismiss();
                }
            }
        });
    }

    // --------------------
    // Methods - Actions
    // --------------------

    private void expandTextsFront(ImageView ivExpandTextsFront, LinearLayout llTextFront) {
        if (addTextFrontIsExpanded) {
            addTextFrontIsExpanded = false;
            ivExpandTextsFront.setImageResource(R.drawable.ic_expand_more_black_36dp);
            llTextFront.startAnimation(ViewUtil.collapse(getActivity(), llTextFront));
        } else {
            addTextFrontIsExpanded = true;
            ivExpandTextsFront.setImageResource(R.drawable.ic_expand_less_black_36dp);
            llTextFront.startAnimation(ViewUtil.expand(getActivity(), llTextFront));
        }
    }

    private void expandQuiz(LinearLayout llQuiz) {
        if (addQuizIsExpanded) {
            addQuizIsExpanded = false;
            llQuiz.startAnimation(ViewUtil.collapse(getActivity(), llQuiz));
        } else {
            addQuizIsExpanded = true;
            llQuiz.startAnimation(ViewUtil.expand(getActivity(), llQuiz));
        }
    }

    private void expandTextsBack(ImageView ivExpandTextsBack, LinearLayout llTextBack) {
        if (addTextBackIsExpanded) {
            addTextBackIsExpanded = false;
            ivExpandTextsBack.setImageResource(R.drawable.ic_expand_more_black_36dp);
            llTextBack.startAnimation(ViewUtil.collapse(getActivity(), llTextBack));
        } else {
            addTextBackIsExpanded = true;
            ivExpandTextsBack.setImageResource(R.drawable.ic_expand_less_black_36dp);
            llTextBack.startAnimation(ViewUtil.expand(getActivity(), llTextBack));
        }
    }

    private void expandTags(LinearLayout llTags) {
        if (addTagsIsExpanded) {
            addTagsIsExpanded = false;
            llTags.startAnimation(ViewUtil.collapse(getActivity(), llTags));
        } else {
            addTagsIsExpanded = true;
            llTags.startAnimation(ViewUtil.expand(getActivity(), llTags));
        }
    }

    private void expandTemplates(LinearLayout llTemplates) {
        if (useTemplateIsExpanded) {
            useTemplateIsExpanded = false;
            llTemplates.startAnimation(ViewUtil.collapse(getActivity(), llTemplates));
        } else {
            useTemplateIsExpanded = true;
            llTemplates.startAnimation(ViewUtil.expand(getActivity(), llTemplates));
        }
    }

    /**
     * Translates text from @param{etFrom} and writes it into @param{etTo} according to languages set in @param{stack}
     *
     * @param languageFrom source language
     * @param languageTo   target language
     * @param etFrom       source edit text
     * @param etTo         target edit text
     */
    private void translate(String languageFrom, String languageTo, EditText etFrom, EditText etTo) {
        try {
            Resources res = getActivity().getResources();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String translatorApiSecret = prefs.getString(res.getString(R.string.pref_translator_api_secret), null);

            AccessControlItem accessControlItem = new MicrosoftAccessControlItemTask().execute(res.getString(R.string.pref_translator_client_id), translatorApiSecret).get();

            if (accessControlItem != null && accessControlItem.getAccess_token() != null) {
                String translatedText = new MicrosoftTranslatorTask().execute(accessControlItem.getAccess_token(), languageFrom, languageTo, etFrom.getText().toString()).get();
                etTo.setText(translatedText);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void addText(TableLayout tblText) {
        TableRow row = (TableRow) tblText.getChildAt(tblText.getChildCount() - 1);

        if (row == null || row.getChildCount() < 1 || !(row.getChildAt(0) instanceof EditText) || (row.getChildAt(0) instanceof EditText && !((EditText) row.getChildAt(0)).getText().toString().isEmpty())) {
            final TableRow tr = new TableRow(getActivity());
            final EditText etText = new EditText(getActivity());
            tr.addView(etText);
            etText.setHint(R.string.new_text);
            etText.requestFocus();
            tblText.addView(tr, tblText.getChildCount());
        }
    }

    private void addAnswer(TableLayout tblQuiz) {
        TableRow trLast = (TableRow) tblQuiz.getChildAt(tblQuiz.getChildCount() - 1);

        if (trLast == null || trLast.getChildCount() < 2 || !(trLast.getChildAt(1) instanceof EditText) || (trLast.getChildAt(1) instanceof EditText && !((EditText) trLast.getChildAt(1)).getText().toString().isEmpty())) {
            final TableRow tr = new TableRow(getActivity());
            final CheckBox cb = new CheckBox(getActivity());
            final EditText etText = new EditText(getActivity());
            tr.addView(cb);
            tr.addView(etText);
            etText.setHint(R.string.new_answer);
            etText.requestFocus();
            cb.setChecked(true);
            tblQuiz.addView(tr, tblQuiz.getChildCount());
        }
    }

    private void addTag(TableLayout tblTags) {
        TableRow trLast = (TableRow) tblTags.getChildAt(tblTags.getChildCount() - 1);

        if (trLast == null || trLast.getChildCount() < 2 || !(trLast.getChildAt(1) instanceof EditText) || (trLast.getChildAt(1) instanceof EditText && !((EditText) trLast.getChildAt(1)).getText().toString().isEmpty())) {
            final TableRow tr = new TableRow(getActivity());
            final CheckBox cb = new CheckBox(getActivity());
            final EditText etText = new EditText(getActivity());
            tr.addView(cb);
            tr.addView(etText);
            etText.setHint(R.string.new_tag);
            etText.requestFocus();
            cb.setChecked(true);
            tblTags.addView(tr, tblTags.getChildCount());
        }
    }

    private void useTemplate(Card template, EditText etFront, EditText etBack, TableLayout tblTextFront, TableLayout tblTextBack, TableLayout tblTags) {
        ((Vibrator) getActivity().getSystemService(Activity.VIBRATOR_SERVICE)).vibrate(VIBRATION_DURATION);

        CardsController cardsController = CardsController.getInstance();

        // Read values from template
        String frontTitle = "";
        ArrayList<String> textsFront = new ArrayList<>();
        if (template.getSides().size() > 0) {
            if (template.getSides().get(0).contains(EComponentType.TITLE))
                frontTitle = ((Title) template.getSides().get(0).getFirst(EComponentType.TITLE)).getValue();
            for (AComponent c : template.getSides().get(0).getComponents()) {
                if (c instanceof Text) {
                    textsFront.add(((Text) c).getValue());
                }
            }
        }
        String backTitle = "";
        ArrayList<String> textsBack = new ArrayList<>();
        if (template.getSides().size() > 1) {
            if (template.getSides().get(1).contains(EComponentType.TITLE))
                backTitle = ((Title) template.getSides().get(1).getFirst(EComponentType.TITLE)).getValue();
            for (AComponent c : template.getSides().get(1).getComponents()) {
                if (c instanceof Text) {
                    textsBack.add(((Text) c).getValue());
                }
            }
        }
        ArrayList<String> tagsAll = Tag.getValues(cardsController.getTagsAll(getActivity()));
        ArrayList<String> tagsSelected = Tag.getValues(template.getTags());

        // Clear
        tblTextFront.removeAllViews();
        tblTextBack.removeAllViews();
        tblTags.removeAllViews();

        // Fill
        if (frontTitle != null && etFront.getText().toString().isEmpty())
            etFront.setText(frontTitle);

        if (backTitle != null && etBack.getText().toString().isEmpty())
            etBack.setText(backTitle);

        for (String s : textsFront) {
            final TableRow tr = new TableRow(getActivity());
            final EditText etText = new EditText(getActivity());
            tr.addView(etText);
            etText.setText(s);
            tblTextFront.addView(tr, tblTextFront.getChildCount());
        }

        for (String s : textsBack) {
            final TableRow tr = new TableRow(getActivity());
            final EditText etText = new EditText(getActivity());
            tr.addView(etText);
            etText.setText(s);
            tblTextBack.addView(tr, tblTextBack.getChildCount());
        }

        if (tagsAll != null) {
            for (final String tag : tagsAll) {
                if (tag != null && !tag.equals(getActivity().getResources().getString(R.string.no_tag))) {
                    final TableRow tr = new TableRow(getActivity());
                    final CheckBox cb = new CheckBox(getActivity());
                    final TextView tvText = new TextView(getActivity());

                    tr.addView(cb);
                    tr.addView(tvText);

                    if (tagsSelected != null && tagsSelected.contains(tag))
                        cb.setChecked(true);

                    tvText.setText(tag);
                    tvText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cb.toggle();
                        }
                    });

                    tblTags.addView(tr, tblTags.getChildCount());
                }
            }
        }
    }

    // --------------------
    // Methods
    // --------------------

    private List<String> getTexts(TableLayout tblTexts) {
        List<String> texts = new ArrayList<>();

        for (int i = 0; i < tblTexts.getChildCount(); i++) {
            if (tblTexts.getChildAt(i) instanceof TableRow) {
                TableRow row = (TableRow) tblTexts.getChildAt(i);

                if (row.getChildCount() > 0 && row.getChildAt(0) instanceof EditText && !((EditText) row.getChildAt(0)).getText().toString().isEmpty()) {
                    texts.add(((EditText) row.getChildAt(0)).getText().toString());
                }
            }
        }

        return texts;
    }

    private List<Answer> getAnswers(TableLayout tblQuiz) {
        List<Answer> selectedAnswers = new ArrayList<>();

        for (int i = 0; i < tblQuiz.getChildCount(); i++) {
            if (tblQuiz.getChildAt(i) instanceof TableRow) {
                TableRow row = (TableRow) tblQuiz.getChildAt(i);
                String value = (row.getChildCount() > 1 && row.getChildAt(1) instanceof EditText) ? ((EditText) row.getChildAt(1)).getText().toString() : "";
                boolean correct = (row.getChildCount() > 0 && row.getChildAt(0) instanceof CheckBox) && ((CheckBox) row.getChildAt(0)).isChecked();

                if (!value.isEmpty()) {
                    selectedAnswers.add(new Answer(value, correct));
                }
            }
        }

        return selectedAnswers;
    }

    private List<Tag> getSelectedTags(TableLayout tblTags) {
        List<Tag> selectedTags = new ArrayList<>();

        for (int i = 0; i < tblTags.getChildCount(); i++) {
            if (tblTags.getChildAt(i) instanceof TableRow) {
                TableRow row = (TableRow) tblTags.getChildAt(i);
                Tag tag = null;
                if (row.getChildCount() > 1 && row.getChildAt(0) instanceof CheckBox && ((CheckBox) row.getChildAt(0)).isChecked()) {
                    if (row.getChildAt(1) instanceof EditText && !((EditText) row.getChildAt(1)).getText().toString().isEmpty()) {
                        tag = new Tag(((EditText) row.getChildAt(1)).getText().toString());
                    } else if (row.getChildAt(1) instanceof TextView && !((TextView) row.getChildAt(1)).getText().toString().isEmpty()) {
                        tag = new Tag(((TextView) row.getChildAt(1)).getText().toString());
                    }

                    if (tag != null && !tag.containedInList(selectedTags)) {
                        selectedTags.add(tag);
                    }
                }
            }
        }

        return selectedTags;
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onAddCard(String frontTitleValue, List<String> frontTextsValues, String backTitleValue, List<String> backTextsValues, List<Tag> tags, List<Answer> answers);

        void onEditCard(String uuid, String frontTitleValue, List<String> frontTextsValues, String backTitleValue, List<String> backTextsValues, List<Tag> tags, List<Answer> answers);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.ocListener = (OnCompleteListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }
}