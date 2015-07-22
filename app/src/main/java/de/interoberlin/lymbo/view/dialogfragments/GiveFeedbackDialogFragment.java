package de.interoberlin.lymbo.view.dialogfragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;

import de.interoberlin.lymbo.R;
import de.interoberlin.lymbo.controller.App;

public class GiveFeedbackDialogFragment extends DialogFragment {
    private OnGiveFeedbackListener ocListener;

    // --------------------
    // Constructors
    // --------------------

    public GiveFeedbackDialogFragment() {
    }

    // --------------------
    // Methods - Lifecycle
    // --------------------

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.getInstance().setGiveFeedbackDialogActive(true);

        // Load layout
        final View v = View.inflate(getActivity(), R.layout.dialogfragment_give_feedback, null);

        // Load dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle b = getArguments();
        builder.setView(v);
        builder.setTitle(R.string.feedback);

        // Add positive button
        builder.setPositiveButton(R.string.okay, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ocListener.onGiveFeedbackDialogDialogComplete();
                App.getInstance().setGiveFeedbackDialogActive(false);
                dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                App.getInstance().setGiveFeedbackDialogActive(false);
                dismiss();
            }
        });

        return builder.create();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    // --------------------
    // Callback interfaces
    // --------------------

    public interface OnGiveFeedbackListener {
        void onGiveFeedbackDialogDialogComplete();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            this.ocListener = (OnGiveFeedbackListener) activity;
        } catch (final ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnCompleteListener");
        }
    }
}