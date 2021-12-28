package gov.anzong.androidnga.fragment

import sp.phone.mvp.presenter.MessagePostPresenter
import sp.phone.mvp.contract.MessagePostContract
import gov.anzong.androidnga.R
import android.widget.EditText
import android.os.Bundle
import android.view.*
import gov.anzong.androidnga.databinding.FragmentMessagePostBinding
import sp.phone.util.StringUtils

/**
 * Created by Justwen on 2017/5/28.
 */
class MessagePostFragment : BaseMvpFragment<MessagePostPresenter?>(), MessagePostContract.View {
    var binding:FragmentMessagePostBinding? = null

    val mTitleEditor: EditText? get() = binding?.etTitle

    val mRecipientEditor: EditText? get() = binding?.etRecipient

    val mBodyEditor: EditText? get() = binding?.etBody

    val mRecipientPanel: ViewGroup? get() = binding?.panelRecipient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPresenter!!.setPostParam(requireArguments().getParcelable("param"))
    }

    override fun onCreatePresenter(): MessagePostPresenter {
        return MessagePostPresenter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentMessagePostBinding.inflate(inflater, container, false).also{
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //ButterKnife.bind(this, view);
        mBodyEditor!!.requestFocus()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_post_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.send -> {
                val title = mTitleEditor!!.text.toString()
                val recipient = mRecipientEditor!!.text.toString()
                val body = mBodyEditor!!.text.toString()
                if (StringUtils.isEmpty(recipient) && mRecipientPanel!!.isShown) {
                    mRecipientEditor!!.error = "请输入收件人"
                    mRecipientEditor!!.requestFocus()
                } else if (StringUtils.isEmpty(body) && body.length < 6) {
                    mBodyEditor!!.error = "请输入内容或者内容字数少于6"
                    mBodyEditor!!.requestFocus()
                } else {
                    mPresenter!!.commit(title, recipient, body)
                }
            }
        }
        return true
    }

    override fun setRecipient(recipient: String?) {
        mRecipientEditor!!.setText(recipient)
        mRecipientEditor!!.setSelection(recipient?.length ?: 0)
    }

    override fun finish(resultCode: Int) {
        requireActivity().setResult(resultCode)
        requireActivity().finish()
    }

    override fun hideRecipientEditor() {
        mRecipientPanel!!.visibility = View.GONE
    }
}