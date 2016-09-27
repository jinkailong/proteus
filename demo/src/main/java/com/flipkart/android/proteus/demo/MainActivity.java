/*
 * Copyright 2016 Flipkart Internet Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.android.proteus.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.flipkart.android.proteus.builder.LayoutBuilder;
import com.flipkart.android.proteus.providers.Layout;
import com.flipkart.android.proteus.view.ProteusView;
import com.flipkart.android.proteusproto.builder.LayoutBuilderImpl;
import com.flipkart.android.proteusproto.models.ProteusLayout;
import com.flipkart.android.proteusproto.parser.FrameLayoutHandler;
import com.flipkart.android.proteusproto.parser.LinearLayoutHandler;
import com.flipkart.android.proteusproto.parser.TextViewHandler;
import com.flipkart.android.proteusproto.providers.LayoutImpl;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

public class MainActivity extends AppCompatActivity {
    FrameLayout containerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        containerView = (FrameLayout) findViewById(R.id.container);

        Button buttonTestProto = (Button) findViewById(R.id.test_proto);
        buttonTestProto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ProteusLayout.AnyViewOrViewGroup> anyViewOrViewGroups = new ArrayList<>();

                anyViewOrViewGroups.add(generateProteusTextView(-1, 0, "TextView", Color.WHITE, 0, 0, 40, 0, 4, 4, 0, false));
                anyViewOrViewGroups.add(generateProteusTextView(-1, 0, "<b>TextView</b> with <font color=#cc8322><i>HTML</i></font> content.", Color.WHITE, 0, 0, 10, 60, 4, 4, 5, true));
                anyViewOrViewGroups.add(generateProteusLinearHorizontalLayoutWithTextViews());

                ProteusLayout.AnyViewOrViewGroup proteusLayout = generateProteusFrameLayoutWith(generateProteusLinearLayoutVertical(anyViewOrViewGroups));
                byte[] bytes = getBytes(proteusLayout);
                containerView.removeAllViews();
                ProteusView proteusView = makeProteusProtoLayout(bytes);
                containerView.addView((View) proteusView);
            }
        });
    }

    @DebugLog
    private ProteusView makeProteusProtoLayout(byte[] bytes) {
        ProteusLayout.AnyViewOrViewGroup proteusLayoutFromBytes = null;
        try {
            proteusLayoutFromBytes = getProteusLayout(bytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        // get the containerView
        // get the layout from bytes and make layoutImpl
        assert proteusLayoutFromBytes != null;
        Layout layout = new LayoutImpl(proteusLayoutFromBytes);
        LayoutBuilder layoutBuilder = new LayoutBuilderImpl();
        //TODO come back and register handlers when done with implementations
        layoutBuilder.registerHandler("VIEW", null);
        layoutBuilder.registerHandler("VIEWGROUP", null);
        layoutBuilder.registerHandler("FRAMELAYOUT", new FrameLayoutHandler());
        layoutBuilder.registerHandler("LINEARLAYOUT", new LinearLayoutHandler());
        layoutBuilder.registerHandler("TEXTVIEW", new TextViewHandler());
        layoutBuilder.registerHandler("BUTTON", null);
        // get the proteusView from layout
        return layoutBuilder.build(containerView, layout, null, 0, null);
    }

    private ProteusLayout.AnyViewOrViewGroup getProteusLayout(byte[] bytes) throws InvalidProtocolBufferException {
        return ProteusLayout.AnyViewOrViewGroup.parseFrom(bytes);
    }

    private byte[] getBytes(ProteusLayout.AnyViewOrViewGroup proteusLayout) {
        return proteusLayout.toByteArray();
    }

    private ProteusLayout.AnyViewOrViewGroup generateProteusFrameLayoutWith(ProteusLayout.AnyViewOrViewGroup anyViewOrViewGroup) {
        ProteusLayout.LayoutParams frameLayoutParams = ProteusLayout.LayoutParams
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutHeight(-2)
                .setLayoutWidth(-1)
                .build();
        ProteusLayout.View frameLayoutView = ProteusLayout.View
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutParams(frameLayoutParams)
                .setPaddingTop(60)
                .setPaddingBottom(60)
                .setPaddingRight(60)
                .setPaddingLeft(60)
                .setBackgroundColor(Color.WHITE)
                .setElevation(18)
                .build();
        ProteusLayout.ViewGroup frameLayoutViewGroup = ProteusLayout.ViewGroup
                .getDefaultInstance()
                .newBuilderForType()
                .setView(frameLayoutView)
                .addAnyViewOrViewGroup(anyViewOrViewGroup)
                .build();
        ProteusLayout.FrameLayout frameLayout = ProteusLayout.FrameLayout
                .getDefaultInstance()
                .newBuilderForType()
                .setViewGroup(frameLayoutViewGroup)
                .build();
        return ProteusLayout.AnyViewOrViewGroup.getDefaultInstance().newBuilderForType().setFrameLayout(frameLayout).build();
    }

    private ProteusLayout.AnyViewOrViewGroup generateProteusLinearLayoutVertical(List<ProteusLayout.AnyViewOrViewGroup> list) {

        ProteusLayout.LayoutParams linearLayoutParams = ProteusLayout.LayoutParams
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutHeight(-1)
                .setLayoutWidth(-1)
                .build();
        ProteusLayout.View linearLayoutView = ProteusLayout.View
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutParams(linearLayoutParams)
                .build();
        ProteusLayout.ViewGroup linearLayoutViewGroup = ProteusLayout.ViewGroup
                .getDefaultInstance()
                .newBuilderForType()
                .setView(linearLayoutView)
                .addAllAnyViewOrViewGroup(list)
                .build();
        ProteusLayout.LinearLayout linearLayout = ProteusLayout.LinearLayout
                .getDefaultInstance()
                .newBuilderForType()
                .setViewGroup(linearLayoutViewGroup)
                .setOrientation(1)
                .build();
        return ProteusLayout.AnyViewOrViewGroup.getDefaultInstance().newBuilderForType().setLinearLayout(linearLayout).build();
    }

    private ProteusLayout.AnyViewOrViewGroup generateProteusTextView(int width, int height, String text, int backgroundColor, int weight, int textSize,
                                                                     int pT, int pB, int pR, int pL, int gravity, boolean isHtml) {
        ProteusLayout.LayoutParams textViewParams = ProteusLayout.LayoutParams
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutWidth(width)
                .setLayoutHeight(height)
                .setWeight(weight)
                .build();
        ProteusLayout.View textViewView = ProteusLayout.View
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutParams(textViewParams)
                .setPaddingTop(pT)
                .setPaddingBottom(pB)
                .setPaddingRight(pR)
                .setPaddingLeft(pL)
                .build();
        ProteusLayout.TextView textView = ProteusLayout.TextView
                .getDefaultInstance()
                .newBuilderForType()
                .setView(textViewView)
                .setText(text)
                .setIsHtmlText(isHtml)
                .setTextSize(textSize)
                .setTextBackground(backgroundColor)
                .setGravity(gravity)
                .build();
        return ProteusLayout.AnyViewOrViewGroup.getDefaultInstance().newBuilderForType().setTextView(textView).build();
    }

    private ProteusLayout.AnyViewOrViewGroup generateProteusLinearHorizontalLayoutWithTextViews() {
        List<ProteusLayout.AnyViewOrViewGroup> anyViewOrViewGroups = new ArrayList<>();
        anyViewOrViewGroups.add(generateProteusTextView(0, 0, "1", Color.parseColor("#d3d3d3"), 2, 0, 40, 40, 1, 1, 17, false)); // 17 for center
        anyViewOrViewGroups.add(generateProteusTextView(0, 0, "2", Color.parseColor("#d3d3d3"), 3, 0, 40, 40, 1, 1, 17, false));
        anyViewOrViewGroups.add(generateProteusTextView(0, 0, "3", Color.parseColor("#d3d3d3"), 4, 0, 40, 40, 1, 1, 17, false));
        ProteusLayout.LayoutParams linearLayoutParams = ProteusLayout.LayoutParams
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutHeight(-1)
                .setLayoutWidth(-1)
                .build();
        ProteusLayout.View linearLayoutView = ProteusLayout.View
                .getDefaultInstance()
                .newBuilderForType()
                .setLayoutParams(linearLayoutParams)
                .build();
        ProteusLayout.ViewGroup linearLayoutViewGroup = ProteusLayout.ViewGroup
                .getDefaultInstance()
                .newBuilderForType()
                .setView(linearLayoutView)
                .addAllAnyViewOrViewGroup(anyViewOrViewGroups)
                .build();
        ProteusLayout.LinearLayout linearLayout = ProteusLayout
                .LinearLayout.getDefaultInstance()
                .newBuilderForType()
                .setViewGroup(linearLayoutViewGroup)
                .setOrientation(0)
                .build();
        return ProteusLayout.AnyViewOrViewGroup.getDefaultInstance().newBuilderForType().setLinearLayout(linearLayout).build();
    }

}
