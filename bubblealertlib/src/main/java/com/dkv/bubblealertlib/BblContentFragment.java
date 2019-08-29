package com.dkv.bubblealertlib;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Scroller;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * @author DKV.
 */
public class BblContentFragment extends BaseDialogFragment {


    private final static String PARAM_TAG = "PARAM_TAG";
    public TextView txtDialogTitle = null;
    public TextView txtContent = null;
    public TextView btnOk = null;
    public TextView btnCancel = null;
    public TextView btnExit = null;
    public String content, ok, cancel, exit;
    public int btnCount = 0;
    public ClickHandler clickHandler = null;
    public IDialogListener dialogListener = null;
    public EditText edtLibName = null;
    public EditText edtLibName2 = null;
    public String TAG, textContent = null;
    public boolean hasEditText;
    public boolean isMultiLineEditText = false;
    public String hintText = null;
    public String sDialogTitle = null;
    BblDialogManager.BubbleSetUp setup = null;

    public BblContentFragment() {
    }

    public static BblContentFragment newInstance(String TAG) {
        Bundle bundle = new Bundle();
        bundle.putString(PARAM_TAG, TAG);
        BblContentFragment bblContentFragment = new BblContentFragment();
        bblContentFragment.setArguments(bundle);
        return bblContentFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle bundle = getArguments();
        TAG = bundle.getString(PARAM_TAG);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.layout_bbl_content, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        clickHandler = new ClickHandler(this);

        txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
        txtContent = (TextView) view.findViewById(R.id.txtContent);
        btnOk = (TextView) view.findViewById(R.id.btnOk);
        btnExit = (TextView) view.findViewById(R.id.btnExit);
        btnCancel = (TextView) view.findViewById(R.id.btnCancel);
        edtLibName = (EditText) view.findViewById(R.id.edtLibName);
        edtLibName2 = (EditText) view.findViewById(R.id.edtLibName2);

        //btnOk.setOnClickListener(clickHandler);
        btnExit.setOnClickListener(clickHandler);
        btnCancel.setOnClickListener(clickHandler);

        txtContent.setText(content);
        btnOk.setText(ok);
        btnCancel.setText(cancel);
        btnExit.setText(exit);
        //if (!TextUtils.isEmpty(sDialogTitle)) {
        txtDialogTitle.setText(sDialogTitle);
        //}

        if (btnCount == 1) {
            btnCancel.setVisibility(View.GONE);
        } else if (btnCount == 3) {
            btnExit.setVisibility(View.VISIBLE);
        }

        int visibility = hasEditText ? View.VISIBLE : View.GONE;

        /*edtLibName.setVisibility(visibility);
        edtLibName2.setVisibility(visibility);
        if (!TextUtils.isEmpty(textContent)) {

            edtLibName.setText(textContent);
            edtLibName2.setText(textContent);
        }

        if (!TextUtils.isEmpty(hintText)) {

            edtLibName.setHint(hintText);
            edtLibName2.setText(hintText);
        }*/

        /*if (isMultiLineEditText) {
            edtLibName.setMaxLines(1);
            edtLibName.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            edtLibName.setScroller(new Scroller(getContext()));
            edtLibName.setVerticalScrollBarEnabled(true);
            edtLibName.setMovementMethod(new ScrollingMovementMethod());
        }*/

        setup.setUp(this);

    }

    @Override
    public BblContentFragment setContent(String content, String okText, String cancelText, String exitText, String dialogTitle) {
        this.content = content;
        this.ok = okText;
        this.cancel = cancelText;
        this.exit = exitText;
        this.sDialogTitle = dialogTitle;

        if (TextUtils.isEmpty(cancel)) {

            btnCount = 1;
        } else if (!TextUtils.isEmpty(exit)) {

            btnCount = 3;
        }
        return this;
    }

    public BblContentFragment setHasEditText(boolean hasEditText) {
        this.hasEditText = hasEditText;
        return this;
    }

    public BblContentFragment setMultiLine(boolean isMultiLineEditText) {
        this.isMultiLineEditText = isMultiLineEditText;
        return this;
    }

    public BblContentFragment setHintText(String hintText) {
        this.hintText = hintText;
        return this;
    }

    public BblContentFragment setSetUp(BblDialogManager.BubbleSetUp setup) {
        this.setup = setup;
        return this;
    }

    @Override
    public int buttonCount() {
        return btnCount;
    }

    public void performOkClicked() {

        if (hasEditText) {

            dialogListener.onOkClicked(TAG, edtLibName.getText().toString());

        } else {
            if (clickedCallBack != null) {
                clickedCallBack.onOkClicked(TAG);
            }
        }

        dismissCallBack.dismiss();
    }

    private void performCancelClicked() {
        if (hasEditText) {

            dialogListener.onCancelClicked(TAG);

        } else {

            clickedCallBack.onCancelClicked(TAG);
        }

        dismissCallBack.dismiss();
    }


    private void performExitClicked() {

        clickedCallBack.onExitClicked(TAG);


        dismissCallBack.dismiss();
    }

    public BblContentFragment setClickedCallBack(IAlertClickedCallBack clickedCallBack) {
        this.clickedCallBack = clickedCallBack;
        return this;
    }

    public BblContentFragment setDialogListener(IDialogListener listener) {

        this.dialogListener = listener;
        return this;
    }

    public BblContentFragment setTextContent(String textContent) {
        this.textContent = textContent;
        return this;
    }

    private static class ClickHandler implements View.OnClickListener {

        WeakReference<BblContentFragment> bblRef = null;

        public ClickHandler(BblContentFragment bblContentFragment) {

            bblRef = new WeakReference<BblContentFragment>(bblContentFragment);
        }

        @Override
        public void onClick(View view) {

            BblContentFragment bblContentFragment = bblRef.get();


            if (view.getId() == R.id.btnOk) {
                bblContentFragment.performOkClicked();
            } else if (view.getId() == R.id.btnCancel) {
                bblContentFragment.performCancelClicked();
            } else if (view.getId() == R.id.btnExit) {
                bblContentFragment.performExitClicked();
            }


        }
    }

}
