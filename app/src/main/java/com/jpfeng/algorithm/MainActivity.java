package com.jpfeng.algorithm;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llVisual;
    private List<Command> bubbleSort;
    private List<Command> oddSort;
    private List<Command> quickSort;
    private View ivRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int[] arr = new int[]{1, 4, 8, 2, 25, 3, 4, 8, 6, 4, 14, 11, 34, 20, 23, 24, 37, 9, 2, 9, 4, 10};
        llVisual = findViewById(R.id.ll_sort_visual);
        ivRange = findViewById(R.id.iv_sort_range);
        ivRange.setVisibility(View.INVISIBLE);

        findViewById(R.id.tv_sort_bubble).setOnClickListener(v -> resetAndPlay(arr, bubbleSort));
        findViewById(R.id.tv_sort_odd).setOnClickListener(v -> resetAndPlay(arr, oddSort));
        findViewById(R.id.tv_sort_quick).setOnClickListener(v -> resetAndPlay(arr, quickSort));

        llVisual.post(() -> {
            int height = llVisual.getHeight();
            int max = findMax(arr);
            for (int i : arr) {
                FrameLayout barContainer = new FrameLayout(this);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.weight = 1;
                ImageView bar = new ImageView(this);
                FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, (int) (height * ((float) i / max)));
                layoutParams1.gravity = Gravity.BOTTOM;
                bar.setImageResource(R.drawable.shape_sort_bar);
                bar.setScaleType(ImageView.ScaleType.FIT_XY);
                barContainer.addView(bar, layoutParams1);
                llVisual.addView(barContainer, layoutParams);
            }
        });

        bubbleSort = new ArrayList<>();
        bSort(Arrays.copyOf(arr, arr.length));

        oddSort = new ArrayList<>();
        oddSort(Arrays.copyOf(arr, arr.length));

        quickSort = new ArrayList<>();
        qSort(Arrays.copyOf(arr, arr.length), 0, arr.length - 1);
    }

    private void resetAndPlay(int[] arr, List<Command> commandList) {
        int height = llVisual.getHeight();
        int max = findMax(arr);
        for (int i = 0; i < arr.length; i++) {
            FrameLayout container = (FrameLayout) llVisual.getChildAt(i);
            View bar = container.getChildAt(0);
            ViewGroup.LayoutParams layoutParams = bar.getLayoutParams();
            layoutParams.height = (int) (height * ((float) arr[i] / max));
            bar.setLayoutParams(layoutParams);
        }
        playAnim(0, commandList);
    }

    private void playAnim(final int index, List<Command> commandList) {
        if (index < 0 || index >= commandList.size()) {
            ivRange.setVisibility(View.INVISIBLE);
            return;
        }

        int width = llVisual.getWidth() / llVisual.getChildCount();

        Command command = commandList.get(index);
        if (command instanceof Swap) {
            int distance = (((Swap) command).right - ((Swap) command).left) * width;
            AnimatorSet set = new AnimatorSet();

            View leftBar = llVisual.getChildAt(((Swap) command).left);
            float leftX = leftBar.getTranslationX();
            ObjectAnimator animatorL = ObjectAnimator
                    .ofFloat(leftBar, "translationX", leftX, leftX + distance);
            View rightBar = llVisual.getChildAt(((Swap) command).right);
            float rightX = rightBar.getTranslationX();
            ObjectAnimator animatorR = ObjectAnimator
                    .ofFloat(rightBar, "translationX", rightX, rightX - distance);

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    llVisual.removeView(leftBar);
                    leftBar.setTranslationX(0);
                    llVisual.addView(leftBar, ((Swap) command).right);
                    llVisual.removeView(rightBar);
                    rightBar.setTranslationX(0);
                    llVisual.addView(rightBar, ((Swap) command).left);
                    playAnim(index + 1, commandList);
                }
            });
            set.playTogether(animatorL, animatorR);
            set.start();

        } else if (command instanceof Range) {
            ivRange.setVisibility(View.VISIBLE);
            ivRange.setTranslationX(((Range) command).start * width);
            ViewGroup.LayoutParams layoutParams = ivRange.getLayoutParams();
            layoutParams.width = (((Range) command).end - ((Range) command).start + 1) * width;
            ivRange.setLayoutParams(layoutParams);
            ivRange.getHandler().postDelayed(() -> playAnim(index + 1, commandList), 150);
        }
    }

    private int findMax(int[] arr) {
        int max = arr[0];
        for (int i : arr) {
            if (i > max) {
                max = i;
            }
        }
        return max;
    }

    private void bSort(int[] arr) {
        int i, temp, len = arr.length;
        boolean changed;
        do {
            changed = false;
            len -= 1;
            for (i = 0; i < len; i++) {
                bubbleSort.add(new Range(i, i + 1));
                if (arr[i] > arr[i + 1]) {
                    temp = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = temp;
                    changed = true;
                    bubbleSort.add(new Swap(i, i + 1));
                }
            }
        } while (changed);
    }

    private void oddSort(int arr[]) {
        boolean sorted = false;
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < 2; i++) {
                for (int j = i; j < arr.length - 1; j += 2) {
                    oddSort.add(new Range(j, j + 1));
                    if (arr[j] > arr[j + 1]) {
                        int temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;
                        oddSort.add(new Swap(j, j + 1));
                        sorted = false;
                    }
                }
            }
        }
    }

    private void qSort(int[] arr, int head, int tail) {
        if (head >= tail || arr == null || arr.length <= 1) {
            return;
        }
        int i = head, j = tail, pivot = arr[(head + tail) / 2];
        quickSort.add(new Range(head, tail));
        while (i <= j) {
            while (arr[i] < pivot) {
                ++i;
            }
            while (arr[j] > pivot) {
                --j;
            }
            if (i < j) {
                int t = arr[i];
                arr[i] = arr[j];
                arr[j] = t;
                quickSort.add(new Swap(i, j));
                ++i;
                --j;
            } else if (i == j) {
                ++i;
            }
        }
        qSort(arr, head, i - 1);
        qSort(arr, i, tail);
    }

    private abstract class Command {
    }

    private class Swap extends Command {
        Swap(int left, int right) {
            this.left = left;
            this.right = right;
        }

        int left;
        int right;
    }

    private class Range extends Command {
        Range(int start, int end) {
            this.start = start;
            this.end = end;
        }

        int start;
        int end;
    }
}
