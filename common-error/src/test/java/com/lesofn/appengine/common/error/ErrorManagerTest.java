package com.lesofn.appengine.common.error;

import com.lesofn.appengine.common.error.example.UserLoginErrorCodes;
import com.lesofn.appengine.common.error.model.TreeNode;
import com.lesofn.appengine.common.error.system.HttpCodes;
import com.lesofn.appengine.common.error.system.SystemErrorCodes;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author lishaofeng
 * @version 1.0 Created at: 2022-03-10 12:28
 */
class ErrorManagerTest {

    @Test
    void getAllErrorCodes() {
        SystemErrorCodes.values();
        HttpCodes.values();
        UserLoginErrorCodes.values();
        List<TreeNode> allErrorCodes = ErrorManager.getAllErrorCodes();
        System.out.println(allErrorCodes);

        for (TreeNode treeNode : allErrorCodes) {
            System.out.println("1." + treeNode);
            for (TreeNode node : treeNode.getNodes()) {
                System.out.println("2." + treeNode);
                for (TreeNode left : node.getNodes()) {
                    System.out.println("3." + left);
                }
            }
        }

    }
}