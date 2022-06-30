package com.zcx.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "???";

    private TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try(
                InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        ) {
            String keyword = null;
            while ((keyword = bufferedReader.readLine()) != null) {
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            throw new RuntimeException("加载敏感词文件失败，服务器出现异常");
        }

    }

    private void addKeyword(String keyword) {
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i ++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        TrieNode tempNode = rootNode;
        int begin = 0;
        int position = 0;
        StringBuilder stringBuilder = new StringBuilder();
        while(begin < text.length()) {
            char c = text.charAt(position);
            if (isSymbol((c))) {
                if (tempNode == rootNode) {
                    stringBuilder.append(c);
                    begin ++;
                }
                position ++;
                if (position >= text.length() - 1) {
                    if ((begin >= text.length())) {
                        break;
                    }
                    stringBuilder.append(text.charAt(begin));
                    begin ++;
                    position = begin;
                    tempNode = rootNode;
                }
                continue;
            }
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                stringBuilder.append(text.charAt(begin));
                begin ++;
                position = begin;
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                stringBuilder.append(REPLACEMENT);
                position ++;
                begin = position;
                tempNode = rootNode;
            } else if (position >= text.length() - 1) {
                stringBuilder.append(text.charAt(begin));
                begin ++;
                position = begin;
                tempNode = rootNode;
            } else {
                if (position < text.length() - 1) {
                    position ++;
                }
            }
        }
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    private boolean isSymbol(Character character) {
        return !CharUtils.isAsciiAlphanumeric(character) && (character < 0x2E80 || character > 0x9FFF);
    }

    private class TrieNode {

        private boolean isKeywordEnd;

        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        public void addSubNode(Character character, TrieNode trieNode) {
            subNodes.put(character, trieNode);
        }

        public TrieNode getSubNode(Character character) {
            return subNodes.get(character);
        }
    }

}
