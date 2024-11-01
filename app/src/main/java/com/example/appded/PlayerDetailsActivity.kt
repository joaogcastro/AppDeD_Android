package com.example.appded

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.appded.database.AppDatabase
import com.example.appded.player.PlayerController
import com.example.appded.utils.getStatus
import kotlinx.coroutines.launch

class PlayerDetailsActivity : AppCompatActivity() {
    private lateinit var backButton: Button
    private lateinit var playerStatusTextView: TextView
    private lateinit var database: AppDatabase
    private lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_details)

        // Instancia do db
        database = AppDatabase.getDatabase(applicationContext)
        playerController = PlayerController(database.playerDao())

        // Referencia as views
        playerStatusTextView = findViewById(R.id.playerStatusTextView)
        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Carrega o jogador selecionado a partir do Intent
        val playerId = intent.getIntExtra("PLAYER_ID", -1)
        if (playerId == -1) {
            Toast.makeText(this, "ID de jogador invalido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadPlayer(playerId)
    }

    private fun loadPlayer(playerId: Int) {
        lifecycleScope.launch {
            val currentPlayer = playerController.getPlayerById(playerId)
            if(currentPlayer != null) {
                playerStatusTextView.text = getStatus(currentPlayer)
            } else {
                Toast.makeText(this@PlayerDetailsActivity, "Personagem não encontrado no banco de dados", Toast.LENGTH_SHORT).show()
                this@PlayerDetailsActivity.finish()
            }
        }
    }
}