package com.example.memorygame

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memorygame.State.CLOSE
import com.example.memorygame.State.OPEN

class BoardViewModel(
    private val difficulty: BoardDifficulty,
    private val boardUseCase: BoardUseCase
) : ViewModel() {

    private val maxScore = (difficulty.columns * difficulty.rows) / 2

    private var isWaitingForMatch = false

    private var _isBoardActive = MutableLiveData(true)
    val isBoardActive: LiveData<Boolean>
        get() = _isBoardActive

    private var _firstCardState = MutableLiveData<Pair<Int, State>>()
    val firstCardState: LiveData<Pair<Int, State>>
        get() = _firstCardState

    private var _secondCardState = MutableLiveData<Pair<Int, State>>()
    val secondCardState: LiveData<Pair<Int, State>>
        get() = _secondCardState

    private var _cardList = MutableLiveData<List<Card>>()
    val cardList: LiveData<List<Card>>
        get() = _cardList

    private val _score = MutableLiveData(INITIAL_SCORE)
    val score: LiveData<Int>
        get() = _score

    init {
        getCards()
    }

    private fun getCards() {
        _cardList.value = boardUseCase.getCardsToPlay(maxScore).toMutableList()
    }

    fun getMaxScore() = maxScore

    fun onCardClicked(clickedCard: Card, position: Int) {
        if (clickedCard.state == CLOSE) {
            if (isWaitingForMatch) {
                _secondCardState.value = Pair(position, OPEN)
                verifyMatch()
            } else {
                _firstCardState.value = Pair(position, OPEN)
            }
            isWaitingForMatch = !isWaitingForMatch
        }
    }

    private fun verifyMatch() {
        _isBoardActive.value = false

        val firstPosition = firstCardState.value?.first ?: INVALID_POSITION
        val secondPosition = secondCardState.value?.first ?: INVALID_POSITION

        val firstCard = cardList.value?.get(firstPosition)
        val secondCard = cardList.value?.get(secondPosition)
        if (boardUseCase.verifyMatch(firstCard, secondCard)) {
            val currentScore = score.value ?: 0
            _score.value = currentScore + 1
            _isBoardActive.value = true
        } else {
            Handler().postDelayed({
                _firstCardState.value = Pair(firstPosition, CLOSE)
                _secondCardState.value = Pair(secondPosition, CLOSE)
                _isBoardActive.value = true
            }, REVEAL_WAIT_TIME_MILLIS)
        }
    }

    fun areYouWinner(score: Int): Boolean {
        return boardUseCase.areYouWinner(score, difficulty)
    }

    companion object {
        private const val INITIAL_SCORE = 0
        private const val INVALID_POSITION = -1
        private const val REVEAL_WAIT_TIME_MILLIS = 500L
    }
}
