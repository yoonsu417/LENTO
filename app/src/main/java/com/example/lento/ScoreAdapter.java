package com.example.lento;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import java.io.File;
import java.util.List;
public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {
    private Context context;
    private List<Score> scoreList;

    public ScoreAdapter(Context context, List<Score> scoreList) {
        this.context = context;
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Score score = scoreList.get(position);
        holder.sheetTitle.setText(score.getTitle());
        holder.sheetComposer.setText(score.getComposer());

        // PDF 파일을 비트맵으로 변환하여 이미지뷰에 설정
        try {
            File file = new File(score.getImagePath());
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            if (parcelFileDescriptor != null) {
                PdfRenderer renderer = new PdfRenderer(parcelFileDescriptor);
                if (renderer.getPageCount() > 0) {
                    PdfRenderer.Page page = renderer.openPage(0);
                    Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE); // 비트맵을 흰색으로 초기화
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    holder.sheetImage.setImageBitmap(bitmap);
                    page.close();
                }
                renderer.close();
                parcelFileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView sheetTitle;
        TextView sheetComposer;
        ImageView sheetImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sheetTitle = itemView.findViewById(R.id.sheet_title);
            sheetComposer = itemView.findViewById(R.id.sheet_composer);
            sheetImage = itemView.findViewById(R.id.sheet_image);
        }
    }
}
