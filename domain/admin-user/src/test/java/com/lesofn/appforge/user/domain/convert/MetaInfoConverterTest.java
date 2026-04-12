package com.lesofn.appforge.user.domain.convert;

import com.lesofn.appforge.user.menu.dto.MetaDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetaInfoConverterTest {

    private final MetaInfoConverter converter = new MetaInfoConverter();

    @Test
    void testConvertToDatabaseColumn() {
        MetaDTO metaDTO = new MetaDTO();
        metaDTO.setTitle("Test Title");
        metaDTO.setIcon("test-icon");
        metaDTO.setShowLink(true);
        
        String json = converter.convertToDatabaseColumn(metaDTO);
        
        assertNotNull(json);
        assertTrue(json.contains("Test Title"));
        assertTrue(json.contains("test-icon"));
        assertTrue(json.contains("showLink"));
    }

    @Test
    void testConvertToEntityAttribute() {
        String json = "{\"title\":\"Test Title\",\"icon\":\"test-icon\",\"showLink\":true,\"rank\":1}";
        
        MetaDTO metaDTO = converter.convertToEntityAttribute(json);
        
        assertNotNull(metaDTO);
        assertEquals("Test Title", metaDTO.getTitle());
        assertEquals("test-icon", metaDTO.getIcon());
        assertTrue(metaDTO.getShowLink());
        assertEquals(1, metaDTO.getRank());
    }

    @Test
    void testNullConversion() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute(""));
    }
}