package com.github.walkvoid.zone.ai.db.dao;

import com.github.walkvoid.wvframework.models.BooleanEnum;
import com.github.walkvoid.zone.ai.model.dto.AiBotConfigDTO;
import com.github.walkvoid.zone.ai.db.entity.AiBotConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiBotConfigDAOTest {

    @Test
    void toDtoMaskedHidesPlainSecret() {
        AiBotConfig entity = new AiBotConfig();
        entity.setBotName("供应链排障助手");
        entity.setSecret("abcdefgh");
        entity.setIsEnabled(BooleanEnum.YES);
        AiBotConfigDTO dto = AiBotConfigDAO.toDtoMasked(entity);
        assertEquals("••••efgh", dto.getSecret());
        assertTrue(dto.getHasSecret());
        assertEquals(1, dto.getIsEnabled());
        assertEquals("供应链排障助手", dto.getBotName());
    }
}
