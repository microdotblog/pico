package co.hellocode.micro

import android.content.Intent
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import co.hellocode.micro.newpost.NewPostActivity
import co.hellocode.micro.utils.inflate
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.layout_post_image.view.*
import kotlinx.android.synthetic.main.timeline_item.view.*

class PostViewHolder(parent: ViewGroup, private var canShowConversations: Boolean)
    : BaseViewHolder<Post>(parent, R.layout.timeline_item) {

    //private var view: View = parent.inflate(R.layout.timeline_item, false)
    private var post: Post? = null

    init {
        Log.i("PostViewHolder", "init")
        if (this.canShowConversations) {
            rootView.setOnClickListener {
                postDetailIntent(it)
            }
            rootView.itemText.setOnClickListener {
                postDetailIntent(it)
            }
        }
        rootView.setOnLongClickListener {
            if (post == null) {
                return@setOnLongClickListener false
            }
            newPostIntent(it)
            true
        }
        rootView.itemText.setOnLongClickListener {
            if (post == null) {
                return@setOnLongClickListener false
            }
            newPostIntent(it)
            true
        }
        rootView.avatar.setOnClickListener {
            avatarClick(it)
        }
    }

    private fun avatarClick(view: View) {
        if (this.post?.username == null) {
            return
        }
        val intent = Intent(view.context, ProfileActivity::class.java)
        intent.putExtra("username", this.post?.username)
        view.context.startActivity(intent)
    }

    private fun newPostIntent(view: View) {
        val intent = Intent(view.context, NewPostActivity::class.java)
        intent.putExtra("@string/reply_intent_extra_postID", this.post?.ID)
        intent.putExtra("@string/reply_intent_extra_author", this.post?.username)
        if (this.post?.mentions != null) {
            intent.putStringArrayListExtra("mentions", this.post?.mentions)
        }
        view.context.startActivity(intent)
    }

    private fun postDetailIntent(view: View) {
        val intent = Intent(view.context, ConversationActivity::class.java)
        intent.putExtra("@string/reply_intent_extra_postID", this.post?.ID)
        intent.putExtra("@string/reply_intent_extra_author", this.post?.username)
        if (this.post?.mentions != null) {
            intent.putStringArrayListExtra("@string/reply_intent_extra_mentions", this.post?.mentions)
        }
        view.context.startActivity(intent)
    }

    override fun bindItem(item: Post) {
        Log.i("PostViewHolder", "bindItem")
        this.post = item
        // remove any image views leftover from reusing views
        for (i in 0 until rootView.post_layout.childCount) {
            val v = rootView.post_layout.getChildAt(i)
            if (v is ImageView) {
                rootView.post_layout.removeViewAt(i)
            }
        }
        // and remove user avatar image
        rootView.avatar.setImageDrawable(null)

        rootView.itemText.setOnClickListener { v ->
            if (this.canShowConversations) {
                postDetailIntent(v)
            }
        }

        Log.i("PostViewHolder", "${item.content}, ${item.authorName}")
        rootView.itemText.text = item.getParsedContent(rootView.context)
        rootView.itemText.movementMethod = LinkMovementMethod.getInstance() // make links open in browser when tapped
        rootView.author.text = item.authorName
        rootView.username.text = "@${item.username}"
        if (!item.isConversation) {
            rootView.conversationButton.visibility = View.GONE
        } else {
            rootView.conversationButton.visibility = View.VISIBLE
        }

        rootView.timestamp.text = DateUtils.getRelativeTimeSpanString(rootView.context, item.date.time)

        val picasso = Picasso.get()
//            picasso.setIndicatorsEnabled(true) // Uncomment this line to see coloured corners on images, indicating where they're loading from
        // Red = network, blue = disk, green = memory
        picasso.load(item.authorAvatarURL).transform(CropCircleTransformation()).into(rootView.avatar)

        for (i in item.imageSources) {
            val imageView = LayoutInflater.from(rootView.context).inflate(
                    R.layout.layout_post_image,
                    null,
                    false
            )
            rootView.post_layout.addView(imageView)
            picasso.load(i).into(imageView.post_image)
        }

    }
}