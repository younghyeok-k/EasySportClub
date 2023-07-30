package com.example.test.adapter

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.example.Content
import com.example.test.R

class SportListAdapter(val itemClicked: (Content) -> Unit) :
    ListAdapter<Content, SportListAdapter.ItemViewHolder>(differ) {

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(houseModel: Content) {
            val titleTextView = view.findViewById<TextView>(R.id.titleTextView)
            val priceTextView = view.findViewById<TextView>(R.id.priceTextView)
            val thumbnailImageView = view.findViewById<ImageView>(R.id.thumbnailImageView)

            titleTextView.text = houseModel.name
            priceTextView.text = houseModel.price.toString()

            view.setOnClickListener {
                itemClicked(houseModel)
            }
            Glide
                .with(thumbnailImageView.context)
                .load(houseModel.imgUrl)
                // transeform()을 통해 이미지를 변형시키고, RoundCorners를 통해 모서리를 둥글게 바꿔줌
                .transform(CenterCrop(), RoundedCorners(dpToPx(thumbnailImageView.context, 12)))
                .into(thumbnailImageView)


        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(R.layout.item_house, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
//        holder.itemView.setOnClickListener {
//            itemClickListener.onClick(it, position)
//        }
        holder.bind(currentList[position])


    }

    //    // (2) 리스너 인터페이스
//    interface OnItemClickListener {
//        fun onClick(v: View, position: Int)
//    }
//    // (3) 외부에서 클릭 시 이벤트 설정
//    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
//        this.itemClickListener = onItemClickListener
//    }
//    // (4) setItemClickListener로 설정한 함수 실행
//    private lateinit var itemClickListener : OnItemClickListener
//    // roundCorners는 픽셀 단위로 설정해줘야 하기 때문에 dbToPx()를 이용해 dp를 px로 변환
    private fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }


    companion object {
        val differ = object : DiffUtil.ItemCallback<Content>() {
            override fun areItemsTheSame(oldItem: Content, newItem: Content): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Content, newItem: Content): Boolean {
                return oldItem == newItem
            }
        }
    }
}