package com.example.memoflash.home.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memoflash.R
import com.example.memoflash.core.model.Flashcard
import com.example.memoflash.databinding.ItemFlashcardBinding

class FlashcardsAdapter :
    ListAdapter<Flashcard, FlashcardsAdapter.FlashcardViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class FlashcardViewHolder(
        private val binding: ItemFlashcardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Flashcard, number: Int) {
            binding.txtCardNumber.text =
                binding.root.context.getString(R.string.number_format, number)
            binding.txtQuestion.text = card.question
            binding.txtAnswer.text = card.answer
        }
    }

    private companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Flashcard>() {
            override fun areItemsTheSame(oldItem: Flashcard, newItem: Flashcard): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Flashcard, newItem: Flashcard): Boolean =
                oldItem == newItem
        }
    }
}
