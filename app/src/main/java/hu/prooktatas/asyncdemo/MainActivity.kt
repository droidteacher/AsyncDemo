package hu.prooktatas.asyncdemo

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import java.util.*

interface TaskStateListener {
    fun taskPrepared()
    fun taskProcessing(percent: Int?)
    fun taskFinished(result: String?)
}

class MainActivity : AppCompatActivity(), TaskStateListener {

    private lateinit var textView: TextView
    private lateinit var button: Button
    private lateinit var task: TimeConsumingTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.tvInfo)
        button = findViewById(R.id.btnStartTask)
    }

    fun buttonClicked(v: View) {
//        Az eredeti kod, ami ANR-t okoz, azaz blokkolja a main thread-et!
//        textView.text = getString(R.string.app_state_task_started)
//        for (i in 0 until 20) {
//            try {
//                Thread.sleep(2000)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//        textView.text = (getString(R.string.app_state_task_done))

        task = TimeConsumingTask(this)
        task.execute()
    }

    override fun taskPrepared() {
        textView.text = getString(R.string.app_state_task_started)
        btnStartTask.isEnabled = false
    }

    override fun taskProcessing(percent: Int?) {
        textView.text = "$percent %"
    }

    override fun taskFinished(result: String?) {
        textView.text = getString(R.string.app_state_task_done)
        btnStartTask.isEnabled = true
    }
}

class TimeConsumingTask(private val taskStateListener: TaskStateListener): AsyncTask<Void, Int, String>() {

    private val dummyResult = "KUTYA!"

    // A main thread-en fut!!!
    override fun onPreExecute() {
        Log.d(TAG, "Végrehajtás előtt. Szál: ${Thread.currentThread().name}")
        taskStateListener.taskPrepared()
    }


    // A main thread-en fut! Belsoleg hivodik meg, nem nekunk kell meghivni! Nekunk csak a publishProgress()-t kell meghivni!
    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        Log.d(TAG, "onProgressUpdate() called with ${values.toList()}. Szál: ${Thread.currentThread().name}")
        taskStateListener.taskProcessing(values.first())
    }


    // Háttérszálon fut!!!
    // Amit visszaad, az a task VEGEREDMENYE!
    // Jellemzoen valami kalkulacio,
    // processzalas eredmenye es nem feletelenul String,
    // barmilyen tipus lehet.
    override fun doInBackground(vararg params: Void?): String {
        val fullProcessingTimeInSeconds = 40.0

        for (i in 1..20) {
            try {
                Thread.sleep(2000)
                Log.d(TAG, "Felébredt! Szál: ${Thread.currentThread().name}")
                val percent = (i * 2.0) / fullProcessingTimeInSeconds * 100
                publishProgress(percent.toInt())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return "Finished at ${Date()}. Result: $dummyResult"
    }

    // A main thread-en fut! Azt veszi at parameterul, amit a doInBackground() visszaadott!
    override fun onPostExecute(result: String?) {
        Log.d(TAG, "Befejeződött. Szál: ${Thread.currentThread().name}")
        taskStateListener.taskFinished(result)
    }

}

const val TAG = "KZs"