package com.squadtechs.hdwallpapercollectionadmin.activity_wallpapers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.ImageRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squadtechs.hdwallpapercollectionadmin.R


class WallpaperAdapter(val context: Context, val list: ArrayList<WallpaperModel>) :
    RecyclerView.Adapter<WallpaperAdapter.WallpaperHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpaperHolder {
        return WallpaperHolder(LayoutInflater.from(context).inflate(R.layout.cell_design, parent, false))
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: WallpaperHolder, position: Int) {
        adjustLayout(holder, position)
        inflateValues(holder, position)
        setListener(holder, position)
    }

    private fun setListener(holder: WallpaperHolder, position: Int) {
        holder.touchView.setOnLongClickListener {
            showConfirmDialog(position)
            return@setOnLongClickListener true
        }
    }

    private fun showConfirmDialog(position: Int) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle("Warning!")
        alertDialog.setMessage("This wallpaper will be permanently deleted\nAre you sure you want to continue?")
        alertDialog.setPositiveButton("Confirm")
        { dialogInterface, i ->
            val storageRef =
                FirebaseStorage.getInstance().getReference("wallpapers").child("${list[position].wallpaper_key}.jpg")
            val documentRef =
                FirebaseFirestore.getInstance().collection("wallpapers").document(list[position].wallpaper_key)

            storageRef.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    documentRef.delete().addOnCompleteListener { mTask ->
                        if (mTask.isSuccessful) {
                            Toast.makeText(context, "Wallpaper deleted successfully", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, mTask.exception!!.message!!, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, task.exception!!.message!!, Toast.LENGTH_LONG).show()
                }
            }
        }
            .setNegativeButton("Cancel")
            { dialogInterface, i ->
                dialogInterface.cancel()
            }
        alertDialog.show()
    }

    private fun inflateValues(holder: WallpaperHolder, position: Int) {
        holder.txtTitle.visibility = View.GONE
        val cache = DiskBasedCache(context.cacheDir, 0)
        val network = BasicNetwork(HurlStack())
        val requestQueue = RequestQueue(cache, network)
        requestQueue.start()
        val imageRequest = ImageRequest(list[position].wallpaper_image_url, Response.Listener { response ->
            holder.progress.visibility = View.INVISIBLE
            holder.imgCell.setImageBitmap(response)
        },
            0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, Response.ErrorListener { error ->
                holder.progress.visibility = View.INVISIBLE
                Toast.makeText(context, "There was an error loading this image", Toast.LENGTH_LONG).show()
            })
        requestQueue.add(imageRequest)
    }

    private fun adjustLayout(holder: WallpaperHolder, position: Int) {
        val displayMetrics = DisplayMetrics()
        (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        holder.frame.layoutParams = FrameLayout.LayoutParams((width / 2), ((40 * height) / 100))
        if (position % 2 == 0) {
            holder.frame.setPadding(8, 8, 0, 0)
        } else {
            holder.frame.setPadding(8, 8, 8, 0)
        }
    }

    inner class WallpaperHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle: TextView = view.findViewById(R.id.txt_title)
        val frame: FrameLayout = view.findViewById(R.id.main_frame)
        val touchView: View = view.findViewById(R.id.touch_view)
        val imgCell: ImageView = view.findViewById(R.id.img_cell)
        val progress: ProgressBar = view.findViewById(R.id.progress)
    }
}