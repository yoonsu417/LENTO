package com.example.lento;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.transition.Transition;
import android.util.Log;
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

        // PDF 파일을 비트맵으로 변환하여 이미지뷰에 설정 -> 이건 ok
        Log.d("PDF_LOAD", "PDF 파일 경로: " + score.getImagePath());

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
                    Log.d("PDF_LOAD", "비트맵 생성 및 이미지뷰 설정 완료");
                } else {
                    Log.e("PDF_LOAD", "PDF 파일의 페이지 수가 0입니다.");
                }
                renderer.close();
                parcelFileDescriptor.close();
            } else {
                Log.e("PDF_LOAD", "ParcelFileDescriptor가 null입니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PDF_LOAD", "PDF 파일을 로드하는 동안 오류가 발생했습니다: " + e.getMessage());
            // 이미지 로드 실패 시, placeholder 이미지 설정
            holder.sheetImage.setImageResource(R.drawable.scorelisttmp);
        }

        // 각 아이템을 클릭할 때 상세 페이지로 이동
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ScoreDetailActivity.class);
                intent.putExtra("title", score.getTitle());
                intent.putExtra("composer", score.getComposer());
                intent.putExtra("imagePath", score.getImagePath());
                context.startActivity(intent);
            }
        });

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
