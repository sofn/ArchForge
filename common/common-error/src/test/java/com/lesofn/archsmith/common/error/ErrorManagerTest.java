package com.lesofn.archsmith.common.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lesofn.archsmith.common.error.example.TestErrorCodes;
import com.lesofn.archsmith.common.error.example.TestSystemErrorCode;
import com.lesofn.archsmith.common.error.manager.ErrorManager;
import com.lesofn.archsmith.common.error.manager.TreeNode;
import com.lesofn.archsmith.common.error.system.HttpCodes;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author sofn
 * @version 1.0 Created at: 2022-03-10 12:28
 */
class ErrorManagerTest {

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void getAllErrorCodes() {
        TestSystemErrorCode.values();
        HttpCodes.values();
        TestErrorCodes.values();
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
