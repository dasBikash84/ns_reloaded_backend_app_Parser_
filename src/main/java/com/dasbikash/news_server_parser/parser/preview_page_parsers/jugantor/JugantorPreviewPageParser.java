package com.dasbikash.news_server_parser.parser.preview_page_parsers.jugantor;

import com.dasbikash.news_server_parser.parser.preview_page_parsers.PreviewPageParser;
import com.dasbikash.news_server_parser.utils.DisplayUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;



public class JugantorPreviewPageParser extends PreviewPageParser {

    //private static final String TAG = "StackTrace";
    private static final String TAG = "JugEdLoader";

    private static final String MINUTES_AGO_TIME_STRING_BANGLA = "মি. আগে";
    private static final String HOURS_AGO_TIME_STRING_BANGLA = "ঘ. আগে";
    public static final int ONE_HOUR_IN_MILLIS = 3600000;
    public static final int ONE_MINUTE_IN_MILLIS = 60000;

    private final String mSiteBaseAddress = "https://www.jugantor.com";

    @Override
    protected String getSiteBaseAddress() {
        return mSiteBaseAddress;
    }

    @Override
    protected String getArticlePublicationDatetimeFormat() {
        return JugantorPreviewPageParserInfo.ARTICLE_PUBLICATION_DATE_TIME_FORMAT;
    }

    @Override
    protected Elements getPreviewBlocks() {
        return mDocument.select(JugantorPreviewPageParserInfo.ARTICLE_PREVIEW_BLOCK_SELECTOR);
    }

    @Override
    protected String getArticleLink(Element previewBlock) {
        return previewBlock.select(JugantorPreviewPageParserInfo.ARTICLE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(JugantorPreviewPageParserInfo.ARTICLE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticlePreviewImageLink(Element previewBlock) {
        return previewBlock.select(JugantorPreviewPageParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_ELEMENT_SELECTOR).get(0).
                                    attr(JugantorPreviewPageParserInfo.ARTICLE_PREVIEW_IMAGE_LINK_TEXT_SELECTOR_TAG);
    }

    @Override
    protected String getArticleTitle(Element previewBlock) {
        return previewBlock.select(JugantorPreviewPageParserInfo.ARTICLE_TITLE_ELEMENT_SELECTOR).
                                    get(0).text();
    }

    @Override
    protected String getArticlePublicationDateString(Element previewBlock) {
        return null;
    }

    @Override
    protected String processArticlePreviewImageLink(String previewImageLink) {
        if (!previewImageLink.matches(".+?url\\(.+?\\).+")){
            return null;
        }
        previewImageLink = (previewImageLink.split("url\\("))[1];
        previewImageLink = (previewImageLink.split("\\)"))[0];
        //return previewImageLink;
        return super.processArticlePreviewImageLink(previewImageLink);
    }

    @Override
    protected Long getArticlePublicationTimeStamp(Element previewBlock) {
        String articlePublicationDateString = previewBlock.select(JugantorPreviewPageParserInfo.ARTICLE_PUBLICATION_DATE_ELEMENT_SELECTOR).
                                                get(0).text();
        return processDateString(articlePublicationDateString,mSimpleDateFormat);
    }

    private long processDateString(String articlePublicationDateString, SimpleDateFormat simpleDateFormat) {
        //Log.d(TAG, "processDateString: articlePublicationDateString: "+articlePublicationDateString);
        if (articlePublicationDateString.contains(HOURS_AGO_TIME_STRING_BANGLA)){
            articlePublicationDateString =
                    articlePublicationDateString.replace(HOURS_AGO_TIME_STRING_BANGLA,"").trim();
            int agoHour = 0;
            try {
                agoHour = Integer.decode(articlePublicationDateString);
                //Log.d(TAG, "processDateString: agoHour: " + agoHour);
                Calendar publicationTime = Calendar.getInstance();
                publicationTime.setTimeInMillis(publicationTime.getTimeInMillis() - agoHour* ONE_HOUR_IN_MILLIS);
                return publicationTime.getTimeInMillis();
            }catch (Exception ex){
                //Log.d(TAG, "processDateString: Error: "+ex.getMessage());
                return 0L;
            }
        }else if(articlePublicationDateString.contains(MINUTES_AGO_TIME_STRING_BANGLA)){
            articlePublicationDateString =
                    articlePublicationDateString.replace(MINUTES_AGO_TIME_STRING_BANGLA,"").trim();
            int agoMinutes = 0;
            try {
                agoMinutes = Integer.decode(articlePublicationDateString);
                //Log.d(TAG, "processDateString: agoMinutes: " + agoMinutes);
                Calendar publicationTime = Calendar.getInstance();
                publicationTime.setTimeInMillis(publicationTime.getTimeInMillis() - agoMinutes* ONE_MINUTE_IN_MILLIS);
                return publicationTime.getTimeInMillis();
            }catch (Exception ex){
                //Log.d(TAG, "processDateString: Error: "+ex.getMessage());
                return 0L;
            }
        } else{
            //Log.d(TAG, "processDateString: GeneralTimeString: "+articlePublicationDateString);
            articlePublicationDateString = DisplayUtils.banglaToEnglishDateString(articlePublicationDateString).trim();
            //Log.d(TAG, "processDateString: inEnglish: "+articlePublicationDateString);
            Calendar publicationTime = Calendar.getInstance();
            publicationTime.setTimeZone(simpleDateFormat.getTimeZone());
            try {
                publicationTime.setTime(simpleDateFormat.parse(articlePublicationDateString));
                return publicationTime.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0L;
            }
        }
    }

}