package com.kpfu.itis.mycamera.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.di.Injector
import com.kpfu.itis.mycamera.presentation.lists.PresetsAdapter
import com.kpfu.itis.mycamera.presentation.presenter.PresetsListPresenter
import com.kpfu.itis.mycamera.presentation.view.PresetsListView
import kotlinx.android.synthetic.main.fragment_presets_list.*
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter
import javax.inject.Inject
import javax.inject.Provider

class PresetsFragment : MvpAppCompatFragment(), PresetsListView {

    @Inject
    lateinit var presenterProvider: Provider<PresetsListPresenter>

    private val presenter: PresetsListPresenter by moxyPresenter {
        presenterProvider.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Injector.appComponent.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_presets_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.initRecyclerView()
        setRecyclerViewItemTouchListener()
        back_button.setOnClickListener {
            presenter.backToCamera()
        }
    }

    override fun initRecyclerView() {
        presetsRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = PresetsAdapter { preset -> presenter.setPreset(preset) }
        }
        (presetsRecycler.adapter as PresetsAdapter).submitList(presenter.getSavedPresets())
    }

    override fun setRecyclerViewItemTouchListener() {
        val itemTouchCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                viewHolder1: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val index = viewHolder.adapterPosition
                (presetsRecycler.adapter as PresetsAdapter).submitList(
                    presenter.updatePresetsPreference(
                        index
                    )
                )
            }
        }
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        presetsRecycler.addItemDecoration(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(presetsRecycler)
    }

    override fun setUpPreset(presetArray: IntArray) {
        Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            R.id.action_presetList_to_camera,
            bundleOf(CameraFragment.PRESET_ARGUMENT to presetArray)
        )
    }

    override fun backToCamera() {
        Navigation.findNavController(requireActivity(), R.id.fragment_container).navigate(
            R.id.action_presetList_to_camera
        )
    }
}
