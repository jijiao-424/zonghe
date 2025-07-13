package com.hui.tallybox.frag_record;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.hui.tallybox.R;
import com.hui.tallybox.db.TypeBean;
import java.util.List;

public class TypeBaseAdapter extends BaseAdapter {
    Context context;
    List<TypeBean>mDatas;
    int selectPos = 0;  //选中位置
    public TypeBaseAdapter(Context context, List<TypeBean> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recordfrag_gv, parent, false);
            holder = new ViewHolder();
            holder.iv = convertView.findViewById(R.id.item_recordfrag_iv);
            holder.tv = convertView.findViewById(R.id.item_recordfrag_tv);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TypeBean typeBean = mDatas.get(position);
        holder.tv.setText(typeBean.getTypename());

        // 【防弹版修复】使用 try-catch 捕获所有可能的资源未找到异常
        try {
            if (selectPos == position) {
                int selectedImageId = typeBean.getSimageId();
                // 我们不再需要 if (id != 0) 判断，因为 try-catch 更强大
                holder.iv.setImageResource(selectedImageId);
            } else {
                int normalImageId = typeBean.getImageId();
                holder.iv.setImageResource(normalImageId);
            }
        } catch (android.content.res.Resources.NotFoundException e) {
            // 【关键】如果发生异常，应用不会闪退！
            // 我们会在这里打印一条日志，告诉我们是哪个数据出了问题
            android.util.Log.e("TypeAdapter_Error",
                    "资源未找到！问题发生在分类: '" + typeBean.getTypename() +
                            "', 位置: " + position);

            // （可选）同时，我们可以给它设置一个默认的“错误”图标
            // holder.iv.setImageResource(R.drawable.ic_error_placeholder);
        }

        return convertView;
    }

    // ViewHolder 静态内部类
    static class ViewHolder {
        ImageView iv;
        TextView tv;
    }
}
