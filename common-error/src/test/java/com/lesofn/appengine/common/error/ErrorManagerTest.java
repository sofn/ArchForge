package com.lesofn.appengine.common.error;

import com.lesofn.appengine.common.error.example.LoginErrorCodes;
import com.lesofn.appengine.common.error.manager.ErrorManager;
import com.lesofn.appengine.common.error.manager.TreeNode;
import com.lesofn.appengine.common.error.system.HttpCodes;
import com.lesofn.appengine.common.error.system.SystemErrorCodes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-10 12:28
 */
class ErrorManagerTest {

    @Test
    void getAllErrorCodes() {
        SystemErrorCodes.values();
        HttpCodes.values();
        LoginErrorCodes.values();
        List<TreeNode> allErrorCodes = ErrorManager.getAllErrorCodes();
        System.out.println(allErrorCodes);
        assertEquals(2, allErrorCodes.size());

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