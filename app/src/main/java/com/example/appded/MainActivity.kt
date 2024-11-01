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
    private lateinit var seeDetailsButton: Button
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

        seeDetailsButton = findViewById(R.id.seeDetails)
        seeDetailsButton.visibility = View.GONE
        seeDetailsButton.setOnClickListener {
            val intent = Intent(this, PlayerDetailsActivity::class.java)
            intent.putExtra("PLAYER_ID", selectedPlayer?.id)
            startActivity(intent)
        }

        editPlayerButton = findViewById(R.id.editPlayerButton)
        editPlayerButton.visibility = View.GONE
        editPlayerButton.setOnClickListener {
            val intent = Intent(this, EditPlayerActivity::class.java)
            intent.putExtra("PLAYER_ID", selectedPlayer?.id)
            startActivity(intent)
        }

        deletePlayerButton = findViewById(R.id.deletePlayerButton)
        deletePlayerButton.visibility = View.GONE
        deletePlayerButton.setOnClickListener {
            if (selectedPlayer == null) {
                Toast.makeText(this@MainActivity, "Nenhum jogador selecionado", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    if(playerController.delete(selectedPlayer)) {
                        Toast.makeText(this@MainActivity, "Jogador ${selectedPlayer!!.name} deletado com sucesso", Toast.LENGTH_SHORT).show()
                        selectedPlayer = null
                        editPlayerButton.visibility = View.GONE
                        deletePlayerButton.visibility = View.GONE
                        seeDetailsButton.visibility = View.GONE
                        loadPlayers()
                    } else {
                        Toast.makeText(this@MainActivity, "Erro ao deletar jogador", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Erro ao deletar jogador: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
		
		// List dinamica que mostra os players na tela
        playersListView = findViewById(R.id.playersListView)
        loadPlayers()
    }

    override fun onResume() {
        super.onResume()
        seeDetailsButton.visibility = View.GONE
        editPlayerButton.visibility = View.GONE
        deletePlayerButton.visibility = View.GONE
        selectedPlayer = null
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
        val emptyListMessage = findViewById<TextView>(R.id.emptyListMessage)

        if (playersList.isEmpty()) {
            emptyListMessage.visibility = View.VISIBLE // Exibe a mensagem
            playersListView.visibility = View.GONE // Esconde a lista
            return
        } else {
            emptyListMessage.visibility = View.GONE // Esconde a mensagem
            playersListView.visibility = View.VISIBLE // Exibe a lista
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
            seeDetailsButton.visibility = View.VISIBLE
            editPlayerButton.visibility = View.VISIBLE
            deletePlayerButton.visibility = View.VISIBLE
        }
    }
}