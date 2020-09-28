package com.kpfu.itis.mycamera.presentation.fragments

import android.content.Intent
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.kpfu.itis.mycamera.BuildConfig
import com.kpfu.itis.mycamera.R
import com.kpfu.itis.mycamera.utils.padWithDisplayCutout
import com.kpfu.itis.mycamera.utils.showImmersive
import java.io.File
import java.util.*

val EXTENSION_WHITELIST = arrayOf("JPG")

class GalleryFragment internal constructor() : Fragment() {

    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>

    inner class MediaPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        val rootDirectory = File(args.rootDirectory)
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase(Locale.ROOT))
        }?.sortedDescending()?.toMutableList() ?: mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mediaList.isEmpty()) {
            view.findViewById<ImageButton>(R.id.delete_button).isEnabled = false
            view.findViewById<ImageButton>(R.id.share_button).isEnabled = false
        }

        val mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container).navigateUp()
        }

        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                val intent = Intent().apply {
                    val mediaType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(mediaFile.extension)
                    val uri = FileProvider.getUriForFile(
                        view.context, BuildConfig.APPLICATION_ID + ".provider", mediaFile
                    )
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mediaType
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }

        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {

            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_dialog))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ ->

                        mediaFile.delete()

                        MediaScannerConnection.scanFile(
                            view.context, arrayOf(mediaFile.absolutePath), null, null
                        )

                        mediaList.removeAt(mediaViewPager.currentItem)
                        mediaViewPager.adapter?.notifyDataSetChanged()

                        if (mediaList.isEmpty()) {
                            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                                .navigate(
                                    R.id.action_gallery_to_camera,
                                    bundleOf("LENS" to args.previousLens.toInt())
                                )
                        }

                    }

                    .setNegativeButton(android.R.string.no, null)
                    .create().showImmersive()
            }
        }
    }
}
