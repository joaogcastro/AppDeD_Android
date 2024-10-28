package com.example.appded

import AppDatabase
import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import races.*

class MainActivity : AppCompatActivity() {

    private lateinit var nameInput: EditText
    private lateinit var createPlayerButton: Button
    private lateinit var selectRaceButton: Button
    private lateinit var selectAbilitiesButton: Button
    private lateinit var savePlayerButton: Button
    private lateinit var editPlayerButton: Button
    private lateinit var deletePlayerButton: Button
    private lateinit var displayPlayersButton: Button
    private lateinit var playerStatusTextView: TextView
    private var selectedRace: Race? = null
    private val playerBuilder = PlayerBuilder()
    private val player = Player()
    private lateinit var database: AppDatabase
    private var currentPlayerId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeDatabase()
        initializeUI()
        setButtonListeners()
    }

    private fun initializeDatabase() {
        try {
            database = AppDatabase.getDatabase(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Erro ao iniciar o banco de dados: ${e.message}")
            Toast.makeText(this, "Erro ao iniciar o banco de dados.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeUI() {
        nameInput = findViewById(R.id.nameInput)
        createPlayerButton = findViewById(R.id.createPlayerButton)
        selectRaceButton = findViewById(R.id.selectRaceButton)
        selectAbilitiesButton = findViewById(R.id.selectAbilitiesButton)
        savePlayerButton = findViewById(R.id.savePlayerButton)
        editPlayerButton = findViewById(R.id.editPlayerButton)
        deletePlayerButton = findViewById(R.id.deletePlayerButton)
        displayPlayersButton = findViewById(R.id.displayPlayersButton)
        playerStatusTextView = findViewById(R.id.playerStatusTextView)

        Log.d("MainActivity", "Inicializando UI")
        nameInput = findViewById(R.id.nameInput)
        Log.d("MainActivity", "nameInput inicializado")
    }

    private fun setButtonListeners() {
        selectRaceButton.setOnClickListener { showRaceSelectionDialog() }
        selectAbilitiesButton.setOnClickListener { showAbilityValueInputDialog() }
        createPlayerButton.setOnClickListener { createPlayer() }
        savePlayerButton.setOnClickListener { savePlayer() }
        editPlayerButton.setOnClickListener { editPlayer() }
        deletePlayerButton.setOnClickListener { deletePlayer() }
        displayPlayersButton.setOnClickListener { displayPlayers() }
    }

    private fun createPlayer() {
        val playerName = nameInput.text.toString()
        if (playerName.isEmpty() || selectedRace == null) {
            Toast.makeText(this, "Por favor, insira um nome e selecione uma raça.", Toast.LENGTH_SHORT).show()
            return
        }
        player.apply {
            name = playerName
            race = selectedRace // Atribuindo a instância de Race diretamente
            playerBuilder.setRaceModifiers(this)
            playerBuilder.setHealthPoints(this)
        }
        playerStatusTextView.text = playerBuilder.getStatus(player)
    }


    private fun showRaceSelectionDialog() {
        val races = playerBuilder.races
        val raceNames = races.map { it.name }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Selecione uma Raça")
            .setItems(raceNames) { _, which ->
                selectedRace = races[which]
                Toast.makeText(this, "Raça selecionada: ${selectedRace?.name}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showAbilityValueInputDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Insira os Valores das Habilidades")

        val abilities = playerBuilder.abilitiesSample
        val abilityInput = Array(abilities.size) { EditText(this) }
        val remainingPointsTextView = TextView(this)

        abilities.forEachIndexed { index, ability ->
            abilityInput[index].apply {
                hint = "$ability (8-15)"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        updateRemainingPoints(abilityInput, remainingPointsTextView)
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }

        builder.setView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            abilityInput.forEach { addView(it) }
            addView(remainingPointsTextView)
        })

        builder.setPositiveButton("OK") { _, _ -> assignAbilities(abilities, abilityInput) }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    private fun updateRemainingPoints(abilityInput: Array<EditText>, remainingPointsTextView: TextView) {
        val totalPointsSpent = abilityInput.sumOf { it.text.toString().toIntOrNull()?.let { value -> playerBuilder.pointCost[value] ?: 0 } ?: 0 }
        val pointsRemaining = playerBuilder.pointBuyBalance - totalPointsSpent
        remainingPointsTextView.text = "Pontos Restantes: $pointsRemaining"
    }

    private fun assignAbilities(abilities: Array<String>, abilityInput: Array<EditText>) {
        val finalValues = mutableMapOf<String, Int>()

        for (i in abilities.indices) {
            val abilityValue = abilityInput[i].text.toString().toIntOrNull()
            if (abilityValue == null || abilityValue < 8 || abilityValue > 15) {
                Toast.makeText(this, "Valor para ${abilities[i]} deve ser entre 8 e 15.", Toast.LENGTH_SHORT).show()
                return
            }
            finalValues[abilities[i]] = abilityValue
        }

        if (abilityInput.sumOf { it.text.toString().toIntOrNull() ?: 0 } > 27) {
            Toast.makeText(this, "Erro: O total de pontos não pode ultrapassar 27.", Toast.LENGTH_SHORT).show()
        } else {
            playerBuilder.assignAbilities(player, finalValues)
            Toast.makeText(this, "Habilidades definidas!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePlayer() {
        lifecycleScope.launch {
            try {
                if (currentPlayerId == null) {
                    database.playerDao().insert(player)
                    Toast.makeText(this@MainActivity, "Personagem salvo!", Toast.LENGTH_SHORT).show()
                } else {
                    database.playerDao().update(player)
                    Toast.makeText(this@MainActivity, "Personagem atualizado!", Toast.LENGTH_SHORT).show()
                }
                clearInputFields()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao salvar o personagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editPlayer() {
        lifecycleScope.launch {
            try {
                val players = database.playerDao().getAllPlayers()
                val playerNames = players.map { it.name }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Escolha um personagem para editar")
                    .setItems(playerNames) { _, which ->
                        val selectedPlayer = players[which]
                        currentPlayerId = selectedPlayer.id

                        player.apply {
                            name = selectedPlayer.name
                            race = selectedPlayer.race // Atribuindo a instância de Race diretamente
                            abilities = Gson().fromJson(selectedPlayer.abilities.toString(), object : TypeToken<MutableMap<String, Int>>() {}.type)
                            healthPoints = selectedPlayer.healthPoints
                        }

                        nameInput.setText(player.name)
                        playerStatusTextView.text = playerBuilder.getStatus(player)
                        Toast.makeText(this@MainActivity, "Personagem carregado para edição!", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao editar o personagem: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun deletePlayer() {
        lifecycleScope.launch {
            try {
                val players = database.playerDao().getAllPlayers()
                val playerNames = players.map { it.name }.toTypedArray()

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Escolha um personagem para excluir")
                    .setItems(playerNames) { _, which ->
                        confirmPlayerDeletion(players[which])
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao carregar personagens: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmPlayerDeletion(selectedPlayer: Player) {
        AlertDialog.Builder(this)
            .setTitle("Confirmação")
            .setMessage("Você tem certeza que deseja excluir ${selectedPlayer.name}?")
            .setPositiveButton("Sim") { _, _ ->
                lifecycleScope.launch {
                    try {
                        database.playerDao().delete(selectedPlayer)
                        Toast.makeText(this@MainActivity, "Personagem excluído!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Erro ao excluir o personagem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun displayPlayers() {
        lifecycleScope.launch {
            try {
                val players = database.playerDao().getAllPlayers()
                val playerNames = players.joinToString("\n") { it.name }
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Personagens Salvos")
                    .setMessage(playerNames.ifEmpty { "Nenhum personagem salvo." })
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Erro ao exibir personagens: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearInputFields() {
        nameInput.text.clear()
        selectedRace = null
        player.abilities.clear()
        player.healthPoints = 10
        playerStatusTextView.text = "O status do personagem aparecerá aqui"
        currentPlayerId = null
    }
}
