package com.example.memoflash.home.agregar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.memoflash.core.model.Flashcard
import com.example.memoflash.databinding.ItemEditableFlashcardBinding

class EditableCardsAdapter(
    private val onEdit: (Flashcard) -> Unit,
    private val onDelete: (Flashcard) -> Unit
) : ListAdapter<Flashcard, EditableCardsAdapter.CardViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemEditableFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(
        private val binding: ItemEditableFlashcardBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Flashcard) {
            binding.txtEditableQuestion.text = card.question
            binding.txtEditableAnswer.text = card.answer
            binding.btnEditCard.setOnClickListener { onEdit(card) }
            binding.btnDeleteCard.setOnClickListener { onDelete(card) }
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
