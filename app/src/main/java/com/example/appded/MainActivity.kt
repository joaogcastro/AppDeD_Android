package com.example.appded

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appded.database.AppDatabase
import com.example.appded.player.Player
import com.example.appded.player.PlayerController
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var playerController: PlayerController
    private lateinit var createPlayerButton: Button
    private lateinit var editPlayerButton: Button
    private lateinit var deletePlayerButton: Button
    private lateinit var playersListView: ListView
    private val playersList = mutableListOf<Player>()
    private var selectedPlayer: Player? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = AppDatabase.getDatabase(applicationContext)
        playerController = PlayerController(database.playerDao())

        createPlayerButton = findViewById(R.id.createPlayerButton)
        createPlayerButton.setOnClickListener {
            val intent = Intent(this, CreatePlayerActivity::class.java)
            startActivity(intent)
        }

        editPlayerButton = findViewById(R.id.editPlayerButton)
        editPlayerButton.visibility = View.GONE
        editPlayerButton.setOnClickListener {
            val intent = Intent(this, CreatePlayerActivity::class.java)
            intent.putExtra("PLAYER_ID", selectedPlayer?.id)
            startActivity(intent)
        }

        deletePlayerButton = findViewById(R.id.deletePlayerButton)
        deletePlayerButton.visibility = View.GONE
        deletePlayerButton.setOnClickListener {
            Toast.makeText(this, "Deletar player.", Toast.LENGTH_SHORT).show()
        }

        playersListView = findViewById(R.id.playersListView)
        loadPlayers()
    }

    private fun loadPlayers() {
        lifecycleScope.launch {
            val players = playerController.listAll()
            playersList.clear()
            playersList.addAll(players)
            updateListView()
        }
    }

    private fun updateListView() {
        if (playersList.isEmpty()) {
            Toast.makeText(this, "Nenhum jogador encontrado.", Toast.LENGTH_SHORT).show()
            return
        }

        val playersNamesList = playersList.map { "Nome: ${it.name}, Raça: ${it.race?.name}" }

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, playersNamesList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.setTextColor(Color.WHITE)
                return textView
            }
        }
        playersListView.adapter = adapter

        playersListView.setOnItemClickListener { _, _, position, _ ->
            selectedPlayer = playersList[position]
            Toast.makeText(this, "Selecionado: ${selectedPlayer?.name}", Toast.LENGTH_SHORT).show()
            editPlayerButton.visibility = View.VISIBLE
            deletePlayerButton.visibility = View.VISIBLE
        }
    }
}
