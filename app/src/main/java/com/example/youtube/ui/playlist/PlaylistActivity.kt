package com.example.youtube.ui.playlist

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.youtube.core.network.result.Status
import com.example.youtube.core.ui.base.BaseActivity
import com.example.youtube.data.remote.model.Playlist
import com.example.youtube.databinding.ActivityPlaylistBinding
import com.example.youtube.di.utils.NoConnection
import com.example.youtube.ui.detail.DetailActivity
import com.example.youtube.ui.playlist.adapter.PlaylistAdapter

class PlaylistActivity : BaseActivity<ActivityPlaylistBinding, PlaylistViewModel>() {
    private lateinit var adapter: PlaylistAdapter

    override val viewModel: PlaylistViewModel by lazy {
        ViewModelProvider(this)[PlaylistViewModel::class.java]
    }

    override fun setUI() {
        super.setUI()
        adapter = PlaylistAdapter(this::onClick)
        binding.recyclerView.adapter = adapter
    }

    override fun setupLiveData() {
        super.setupLiveData()
        viewModel.loading.observe(this){
            binding.progressBar.isVisible = it

        }
        viewModel.getPlaylists().observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    binding.recyclerView.adapter = adapter
                    adapter.addList(it.data?.items as List<Playlist.Item>)
                    viewModel.loading.postValue(false)
                }
                Status.ERROR ->{
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                    viewModel.loading.postValue(false)
                }
                Status.LOADING ->{
                    viewModel.loading.postValue(true)

                }
            }
        }
    }

    override fun inflateViewBinding(): ActivityPlaylistBinding {
        return ActivityPlaylistBinding.inflate(layoutInflater)
    }

    private fun onClick(item: Playlist.Item) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DESCRIPTION, item.snippet?.description)
        intent.putExtra(TITLE, item.snippet?.title)
        intent.putExtra(KEY_ID, item.id)
        startActivity(intent)
    }

    override fun checkInternet() {
        super.checkInternet()
        NoConnection(application).observe(this) {
            if (it) {
                binding.internetConnection.visibility = View.VISIBLE
                binding.noConnection.visibility = View.GONE
            } else {
                binding.internetConnection.visibility = View.GONE
                binding.noConnection.visibility = View.VISIBLE
                setupLiveData()
            }
        }
    }

    companion object {
        const val KEY_ID = "key_id"
        const val DESCRIPTION = "DESCRIPTION"
        const val TITLE = "TITLE"
    }
}
