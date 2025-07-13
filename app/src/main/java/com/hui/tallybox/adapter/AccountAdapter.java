package com.hui.tallybox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hui.tallybox.R;
import com.hui.tallybox.db.AccountBean;

import java.util.Calendar;
import java.util.List;

/**
 * AccountAdapter - 主界面账单ListView的核心数据适配器
 *
 * 作用：
 * 这是一个连接数据源 (List<AccountBean>) 与UI展示 (ListView) 的关键桥梁。
 * 它负责告诉ListView有多少条项目需要显示，以及每一条项目应该如何渲染。
 * 采用了ViewHolder模式进行性能优化，避免了在列表滚动时频繁地调用findViewById，从而保证了滑动的流畅性。
 */
public class AccountAdapter extends BaseAdapter {
    // === 成员变量声明 ===

    Context context;                // 上下文对象，用于访问系统资源，如LayoutInflater
    List<AccountBean> mData;        // 数据源，存储了所有需要被显示的账单对象
    LayoutInflater inflater;        // 布局加载器，用于将XML布局文件转换为View对象

    // 用于判断账单日期是否是“今天”的成员变量
    int year, month, day;

    /**
     * 适配器的构造函数
     * 当创建适配器实例时被调用，用于初始化必要的成员变量。
     * @param context 上下文，通常是调用此适配器的Activity或Fragment
     * @param mData 包含所有账单信息的列表
     */
    public AccountAdapter(Context context, List<AccountBean> mData) {
        this.context = context;
        this.mData = mData;
        // 初始化布局加载器，后续在getView方法中会频繁使用
        inflater = LayoutInflater.from(context);

        // 预先获取并存储当天的日期信息，避免在getView中重复获取，提升效率
        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH) + 1; // 注意：Calendar.MONTH 是从0开始的，所以需要加1
        day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 重写getCount方法，告诉ListView总共有多少个数据项需要显示。
     * @return 数据源列表的大小
     */
    @Override
    public int getCount() {
        return mData.size();
    }

    /**
     * 重写getItem方法，根据指定的位置(position)从数据源中获取对应的数据对象。
     * @param position 项目在列表中的索引
     * @return 对应位置的AccountBean对象
     */
    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    /**
     * 重写getItemId方法，返回指定位置项目的稳定ID。
     * 在这里我们简单地使用位置索引作为ID。
     * @param position 项目在列表中的索引
     * @return 位置索引
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 这是整个Adapter最核心的方法。
     * 每当ListView需要显示一个列表项时，这个方法就会被调用。
     * 它负责创建或复用一个View，并用指定位置的数据来填充这个View。
     *
     * @param position    当前需要渲染的项目的位置
     * @param convertView 可复用的旧视图。如果为null，表示需要创建一个新的视图。
     * @param parent      该视图将要被添加到的父ViewGroup，即ListView本身
     * @return 一个配置好数据的、代表单行列表项的View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // 1. ViewHolder模式的应用，实现视图的复用和性能优化
        ViewHolder holder = null;
        if (convertView == null) {
            // 如果convertView为null，说明当前没有可复用的旧视图
            // a. 使用布局加载器，将item_mainlv.xml布局文件实例化成一个View对象
            convertView = inflater.inflate(R.layout.item_mainlv, parent, false);
            // b. 创建一个新的ViewHolder，并让它去查找并持有该布局中所有子控件的引用
            holder = new ViewHolder(convertView);
            // c. 使用setTag()方法，将这个装满了控件引用的holder对象“附加”到convertView上
            convertView.setTag(holder);
        } else {
            // 如果convertView不为null，说明这是一个被滑出屏幕的旧视图，我们可以直接复用它
            // a. 使用getTag()方法，直接从复用的视图上取回之前附加的ViewHolder对象
            //    这样就避免了再次昂贵地调用findViewById()
            holder = (ViewHolder) convertView.getTag();
        }

        // 2. 数据绑定：从数据源中获取当前位置的账单对象
        AccountBean bean = mData.get(position);

        // 3. 将bean对象中的数据，设置到ViewHolder持有的各个UI控件上
        holder.typeIv.setImageResource(bean.getsImageid());  // 设置分类图标
        holder.tyTv.setText(bean.getTypename());              // 设置分类名称
        holder.remarkTv.setText(bean.getRemark());            // 设置备注信息
        holder.moneyTv.setText("￥" + bean.getMoney());        // 设置金额，并添加人民币符号

        // 4. 特殊逻辑处理：判断日期是否为今天，以显示更友好的时间格式
        if (bean.getYear() == year && bean.getMonth() == month && bean.getDay() == day) {
            // 如果是今天的记录，只显示具体时间（例如 "14:30"）
            String time = bean.getTime().split(" ")[1]; // "YYYY-MM-DD 14:30" -> "14:30"
            holder.timeTv.setText("今天 " + time);
        } else {
            // 如果不是今天的记录，则显示完整的日期
            holder.timeTv.setText(bean.getTime());
        }

        // 5. 返回已经配置好数据的视图，ListView将会把它显示在屏幕上
        return convertView;
    }

    /**
     * ViewHolder (视图持有者) 模式的静态内部类实现。
     *
     * 作用：
     * 这是一个轻量级的容器，它的唯一任务就是在创建时，一次性地查找到并持有列表项布局中所有需要操作的子控件的引用。
     * 这样，在getView方法中，我们就可以通过ViewHolder对象直接访问这些控件，而无需在每次列表滚动时都重复调用findViewById()。
     * 这极大地减少了CPU的消耗，是优化ListView性能的黄金标准。
     */
    class ViewHolder {
        // 声明与item_mainlv.xml布局中控件对应的成员变量
        ImageView typeIv;
        TextView tyTv, remarkTv, moneyTv, timeTv;

        /**
         * ViewHolder的构造函数
         * @param view 一个刚刚被实例化的、代表单行列表项的根视图
         */
        public ViewHolder(View view) {
            // 在这里执行所有findViewById的操作，这个过程只会在创建新的ViewHolder时发生一次。
            typeIv = view.findViewById(R.id.item_mainlv_iv);
            tyTv = view.findViewById(R.id.item_mainlv_tv_title);
            remarkTv = view.findViewById(R.id.item_mainlv_tv_remark);
            moneyTv = view.findViewById(R.id.item_mainlv_tv_money);
            timeTv = view.findViewById(R.id.item_mainlv_tv_time);
        }
    }
}