package de.interoberlin.lymbo.view.dialogfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.model.card.Tag;
import de.interoberlin.lymbo.view.controls.RobotoTextView;

public class FilterStacksDialogFragment extends DialogFragment {
    private OnCompleteListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public FilterStacksDialogFragment() {
    }

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialogfragment_filter_stacks, null);
        final TableLayout tblTags = (TableLayout) v.findViewById(R.id.tblTags);
        final TextView tvAll = (TextView) v.findViewById(R.id.tvAll);
        final TextView tvNone = (TextView) v.findViewById(R.id.tvNone);

        // Get arguments
        Bundle bundle = this.getArguments();
        final ArrayList<String> tagsAll = bundle.getStringArrayList(getActivity().getResources().getString(R.string.bundle_tags_all));
        final ArrayList<String> tagsSelected = bundle.getStringArrayList(getActivity().getResources().getString(R.string.bundle_tags_selected));

        for (final String t : tagsAll) {
            final TableRow tr = new TableRow(getActivity());
            final CheckBox cb = new CheckBox(getActivity());
            final RobotoTextView tvText = new RobotoTextView(getActivity());

            tr.addView(cb);
            tr.addView(tvText);

            for (String cs : tagsSelected) {
                if (cs.equals(t)) {
                    cb.setChecked(true);
                }
            }

            tvText.setText(t);
            tvText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cb.toggle();
                }
            });

            tblTags.addView(tr);
        }

        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAllTagsTo(tblTags, true);
            }
        });

        tvNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setAllTagsTo(tblTags, false);
            }
        });

        // Load dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.filter);

        // Add positive button
        builder.setPositiveButton(R.string.okay, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        // Add negative button
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        return builder.create();
    }

    private void setAllTagsTo(TableLayout tblTags, boolean value) {
        for (int i = 0; i < tblTags.getChildCount(); i++) {
            if (tblTags.getChildAt(i) instanceof TableRow) {
                TableRow tr = (TableRow) tblTags.getChildAt(i);

                if (tr.getChildCount() > 0 && tr.getChildAt(0) instanceof CheckBox) {
                    ((CheckBox) tr.getChildAt(0)).setChecked(value);
                }
            }
        }
    }

    // --------------------
    // Callback interfaces
    // --------------------

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        final TableLayout tblTags = (TableLayout) dialog.findViewById(R.id.tblTags);

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Tag> tagsSelected = new ArrayList<>();

                for (int i = 0; i < tblTags.getChildCount(); i++) {
                    final TableRow tr = (TableRow) tblTags.getChildAt(i);
                    final CheckBox cb = (CheckBox) tr.getChildAt(0);
                    final RobotoTextView tvText = (RobotoTextView) tr.getChildAt(1);

                    if (cb.isChecked()) {
                        tagsSelected.add(new Tag(tvText.getText().toString()));
                    }
                }

                ocListener.onTagsSelected(tagsSelected);

                dismiss();
            }
        });
    }

    public interface OnCompleteListener {
        void onTagsSelected(List<Tag> tagsSelected);

    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.ocListener = (OnCompleteListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTagsSelectedListener");
        }
    }
}