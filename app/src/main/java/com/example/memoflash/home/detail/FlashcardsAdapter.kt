package com.example.memoflash.home.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.memoflash.core.model.Flashcard
import com.example.memoflash.databinding.ItemFlashcardBinding

class FlashcardsAdapter : RecyclerView.Adapter<FlashcardsAdapter.FlashcardViewHolder>() {
    private var cards: List<Flashcard> = emptyList()

    fun submitList(newCards: List<Flashcard>) {
        cards = newCards
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        holder.bind(cards[position], position + 1)
    }

    override fun getItemCount(): Int = cards.size

    class FlashcardViewHolder(
        private val binding: ItemFlashcardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Flashcard, number: Int) {
            binding.txtCardNumber.text = number.toString()
            binding.txtQuestion.text = card.question
            binding.txtAnswer.text = card.answer
        }
    }
}
