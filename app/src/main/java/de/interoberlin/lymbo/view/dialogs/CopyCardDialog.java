package de.interoberlin.lymbo.view.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.StacksController;
import de.interoberlin.lymbo.core.model.v1.impl.Stack;

public class CopyCardDialog extends DialogFragment {
    public static final String TAG = CopyCardDialog.class.getSimpleName();

    private List<CheckBox> checkboxes = new ArrayList<>();
    private String targetLymboId = null;

    private OnCompleteListener onCompleteListener;

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StacksController stacksController = StacksController.getInstance();

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialog_copy_card, null);
        final CheckBox cbDeepCopy = (CheckBox) v.findViewById(R.id.cbDeepCopy);
        final TextView tvDeepCopy = (TextView) v.findViewById(R.id.tvDeepCopy);
        final TableLayout tblLymbos = (TableLayout) v.findViewById(R.id.tblLymbos);

        // Get arguments
        Bundle bundle = this.getArguments();
        String sourceLymboId = bundle.getString(getActivity().getResources().getString(R.string.bundle_lymbo_uuid));

        // Fill views with arguments
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setTitle(R.string.copy_card);

        for (final Stack l : stacksController.getStacks()) {
            if (!l.getId().equals(sourceLymboId)) {
                final TableRow tr = new TableRow(getActivity());

                final CheckBox cb = new CheckBox(getActivity());
                final TextView tvText = new TextView(getActivity());

                checkboxes.add(cb);

                tr.addView(cb);
                tr.addView(tvText);

                cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        targetLymboId = l.getId();
                        if (b) {
                            for (CheckBox c : checkboxes) {
                                c.setChecked(false);
                            }

                            cb.setChecked(true);
                        }
                    }
                });

                tvText.setText(l.getTitle());
                tvText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        targetLymboId = l.getId();
                        cb.toggle();
                    }
                });

                tblLymbos.addView(tr);
            }
        }

        // Add actions
        tvDeepCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cbDeepCopy.toggle();
            }
        });

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
        final CheckBox cbDeepCopy = (CheckBox) dialog.findViewById(R.id.cbDeepCopy);

        Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean deepCopy = cbDeepCopy.isChecked();

                onCompleteListener.onCopyCard(targetLymboId, cardUuid, deepCopy);
            }
        });
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnCompleteListener {
        void onCopyCard(String targetLymboId, String cardId, boolean deepCopy);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.onCompleteListener = (OnCompleteListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCopyCardListener");
        }
    }
}