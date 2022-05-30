package com.umrhsn.mmoire.activities

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.umrhsn.mmoire.R
import com.umrhsn.mmoire.adapters.MemoryBoardAdapter
import com.umrhsn.mmoire.models.BoardSize
import com.umrhsn.mmoire.models.BoardSize.*
import com.umrhsn.mmoire.models.MemoryGame
import com.umrhsn.mmoire.models.UserImageList
import com.umrhsn.mmoire.utils.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
    }

    /** declaring [MainActivity] views */
    private lateinit var clRoot: ConstraintLayout
    private lateinit var llGameInfo: LinearLayout
    private lateinit var cvNumMoves: CardView
    private lateinit var cvNumPairs: CardView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var rvBoard: RecyclerView

    /** [boardSize] */
    private var boardSize: BoardSize = SUPER_DUPER_EASY

    /** [memoryGame] */
    private lateinit var memoryGame: MemoryGame

    /** [Firebase] database instance and related vars */
    private val db = Firebase.firestore
    private var gameName: String? = null
    private var customGameImages: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ActionBar with no elevation
        supportActionBar?.elevation = 0f

        // init MainActivity views
        clRoot = findViewById(R.id.clRoot)
        llGameInfo = findViewById(R.id.llGameInfo)
        cvNumMoves = findViewById(R.id.cvNumMoves)
        cvNumPairs = findViewById(R.id.cvNumPairs)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        rvBoard = findViewById(R.id.rvBoard)

        // setting the memory board size and adapter
        setupBoard()
    }

    /** add options menu */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miRefresh -> {
                configureMIRefreshMenuItem()
            }
            R.id.miChooseAnotherSize -> {
                showChooseAnotherSizeDialog()
                return true
            }
            R.id.miCreateCustomGame -> {
                showCreateCustomGameDialog()
                return true
            }
            R.id.miDownloadCustomGame -> {
                showDownloadCustomGameDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** update ui info according to [boardSize] */
    private fun setupBoard() {
        when (boardSize) {
            SUPER_DUPER_EASY -> {
                tvNumMoves.text =
                    getString(R.string.super_duper_easy_text) + " " + getString(R.string.super_duper_easy_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            SUPER_EASY -> {
                tvNumMoves.text =
                    getString(R.string.super_easy_text) + " " + getString(R.string.super_easy_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            EASY -> {
                tvNumMoves.text =
                    getString(R.string.easy_text) + " " + getString(R.string.easy_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            MEDIUM -> {
                tvNumMoves.text =
                    getString(R.string.medium_text) + " " + getString(R.string.medium_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            HARD -> {
                tvNumMoves.text =
                    getString(R.string.hard_text) + " " + getString(R.string.hard_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            SUPER_HARD -> {
                tvNumMoves.text =
                    getString(R.string.super_hard_text) + " " + getString(R.string.super_hard_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
            SUPER_DUPER_HARD -> {
                tvNumMoves.text =
                    getString(R.string.super_duper_hard_text) + " " + getString(R.string.super_duper_hard_size)
                tvNumPairs.text = "Pairs 0 / ${boardSize.getNumPairs()}"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        // setting the memoryGame params
        memoryGame = MemoryGame(boardSize, customGameImages)
        // update recyclerView and setOnClickListener
        rvBoard.adapter = MemoryBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoryBoardAdapter.CardClickListener {
                override fun onCardClicked(position: Int) = updateGameWithFlip(position)
            }
        )
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    /** update game info with progress */
    @SuppressLint("NotifyDataSetChanged")
    private fun updateGameWithFlip(position: Int) {
        // cases are:
        // 1- user clicks on a card after already winning the game. (INVALID)
        // 2- user clicks on an already face-up card. (INVALID)
        // 3- user wins the game.
        if (memoryGame.haveWonGame()) {
            showToastHaveAlreadyWon(this)
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            showToastInvalidMove(this)
            return
        }
        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "found a match! number of pairs found: ${memoryGame.numPairsFound}")
            // progress is shown via color gradient from red (no progress) to green (full progress)
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int // casted to Int because TextView takes Int color values
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if (memoryGame.smoothWin()) {
                showToastSmoothWin(this)
                rainingConfettiLong(clRoot)
                explosionConfettiArray(clRoot)
            } else if (memoryGame.haveWonGame()) {
                showToastYouWon(this)
                rainingConfettiShort(clRoot)
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        rvBoard.post { run { rvBoard.adapter?.notifyDataSetChanged() } }
    }
    /** configure menu item [R.id.miRefresh] */
    private fun configureMIRefreshMenuItem() {
        if (!memoryGame.haveWonGame()) {
            if (memoryGame.getNumMoves() == 0) {
                showToastNothingToRefresh(this)
            } else {
                showAlertDialog(this,
                    "Quit your current game? You will lose all progress",
                    null, null) { setupBoard() }
            }
        } else {
            showAlertDialog(this, "Play another game?", null, null) { setupBoard() }
        }
    }

    /** configure menu item [R.id.miChooseAnotherSize] */
    private fun showChooseAnotherSizeDialog() {
        val boardSizeView =
            LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when (boardSize) { // current board size automatically selected
            SUPER_DUPER_EASY -> radioGroupSize.check(R.id.rbSuperDuperEasy)
            SUPER_EASY -> radioGroupSize.check(R.id.rbSuperEasy)
            EASY -> radioGroupSize.check(R.id.rbEasy)
            MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            HARD -> radioGroupSize.check(R.id.rbHard)
            SUPER_HARD -> radioGroupSize.check(R.id.rbSuperHard)
            SUPER_DUPER_HARD -> radioGroupSize.check(R.id.rbSuperDuperHard)
        }
        showAlertDialog(this, "Choose new size", null, boardSizeView) {
            // set a new value for the board size
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbSuperDuperEasy -> SUPER_DUPER_EASY
                R.id.rbSuperEasy -> SUPER_EASY
                R.id.rbEasy -> EASY
                R.id.rbMedium -> MEDIUM
                R.id.rbHard -> HARD
                R.id.rbSuperHard -> SUPER_HARD
                else -> SUPER_DUPER_HARD
            }
            /** when we call [setupBoard] it might still be using the cached data (the same data in [gameName] and [customGameImages], so the key thing here is to reset the values every time the user goes back to a default game */
            gameName = null
            customGameImages = null
            setupBoard()
        }
    }

    /** configure menu item [R.id.miCreateCustomGame] */
    private fun showCreateCustomGameDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog(this, "Create your own memory board", null, boardSizeView) {
            // set a new value for the board size
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbSuperDuperEasy -> SUPER_DUPER_EASY
                R.id.rbSuperEasy -> SUPER_EASY
                R.id.rbEasy -> EASY
                R.id.rbMedium -> MEDIUM
                R.id.rbHard -> HARD
                R.id.rbSuperHard -> SUPER_HARD
                else -> SUPER_DUPER_HARD
            }
            // navigate to a new activity
            val intent = Intent(this, CreateActivity::class.java).putExtra(EXTRA_BOARD_SIZE,
                desiredBoardSize)
            resultLauncher.launch(intent)
        }
    }

    /** configure menu item [R.id.miDownloadCustomGame] */
    private fun showDownloadCustomGameDialog() {
        val boardDownloadView =
            LayoutInflater.from(this).inflate(R.layout.dialog_board_download, null)
        showAlertDialog(this, "Fetch memory game", null, boardDownloadView) {
            // grab the text of game name that user wants to download
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadGame.text.toString().trim()
            downloadCustomGame(gameToDownload)
        }
    }

    /** download custom game from [FirebaseFirestore] */
    private fun downloadCustomGame(customGameName: String) {
        db
            .collection("games")
            .document(customGameName)
            .get()

            .addOnSuccessListener { document ->
                val userImageList = document.toObject(UserImageList::class.java)
                if (userImageList?.images == null) {
                    Log.e(TAG, "Exception when retrieving game data from Firestore")
                    Toast.makeText(this,
                        "Sorry we couldn't find such game, '$customGameName'",
                        Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                // if we reach this far this means that we actually have a game with that name in the FirebaseFirestore
                // we fetch board info from the database to be passed in the setupBoard function
                val numCards = userImageList.images.size * 2
                boardSize = BoardSize.getByValue(numCards)
                customGameImages = userImageList.images
                userImageList.images.forEach { imageUrl ->
                    Picasso.get().load(imageUrl).fetch()
                }
                Toast.makeText(this, "You're now playing '$customGameName'!", Toast.LENGTH_LONG)
                    .show()
                gameName = customGameName
                setupBoard()
            }

            .addOnFailureListener { exception ->
                Log.e(
                    TAG,
                    "Exception when retrieving game",
                    exception
                )
            }
    }

    /** customized [ActivityResultLauncher] of type [Intent] */
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
                if (customGameName == null) {
                    Log.e(TAG, "Got null custom game name from ")
                    return@registerForActivityResult
                }
                downloadCustomGame(customGameName)
            }
        }

}