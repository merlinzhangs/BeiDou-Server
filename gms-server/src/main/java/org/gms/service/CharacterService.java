package org.gms.service;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.gms.client.Character;
import org.gms.constants.string.ExtendType;
import org.gms.dao.entity.ExtendValueDO;
import org.gms.dao.mapper.ExtendValueMapper;
import org.gms.net.server.Server;
import org.gms.net.server.world.World;
import org.gms.util.I18nUtil;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Objects;

import static org.gms.dao.entity.table.ExtendValueDOTableDef.EXTEND_VALUE_D_O;

@Service
@AllArgsConstructor
public class CharacterService {
    private final ExtendValueMapper extendValueMapper;

    public void updateRate(ExtendValueDO data) {
        checkName(data);
        data.setExtendType(ExtendType.CHARACTER_EXTEND.getType());
        data.setCreateTime(null);
        data.setUpdateTime(new Date(System.currentTimeMillis()));
        extendValueMapper.insertOrUpdateSelective(data);
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    public void resetRate(ExtendValueDO data) {
        checkName(data);
        extendValueMapper.deleteByQuery(QueryWrapper.create()
                .where(EXTEND_VALUE_D_O.EXTEND_ID.eq(data.getExtendId()))
                .and(EXTEND_VALUE_D_O.EXTEND_TYPE.eq(ExtendType.CHARACTER_EXTEND.getType()))
                .and(EXTEND_VALUE_D_O.EXTEND_NAME.eq(data.getExtendName())));
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    public void resetRates(ExtendValueDO data) {
        extendValueMapper.deleteByQuery(QueryWrapper.create()
                .where(EXTEND_VALUE_D_O.EXTEND_ID.eq(data.getExtendId()))
                .and(EXTEND_VALUE_D_O.EXTEND_TYPE.eq(ExtendType.CHARACTER_EXTEND.getType()))
                .and(EXTEND_VALUE_D_O.EXTEND_NAME.in("expRate", "dropRate", "mesoRate")));
        Character character = getCharacter(data);
        character.resetPlayerRates();
        character.setWorldRates();
        character.setCouponRates();
    }

    private void checkName(ExtendValueDO data) {
        // 非法请求篡改其他字段
        if ("expRate".equals(data.getExtendName()) || "dropRate".equals(data.getExtendName()) || "mesoRate".equals(data.getExtendName())) {
            return;
        }
        throw new IllegalArgumentException();
    }


    private Character getCharacter(ExtendValueDO data) {
        for (World world : Server.getInstance().getWorlds()) {
            for (Character character : world.getPlayerStorage().getAllCharacters()) {
                if (ExtendType.isAccount(data.getExtendType()) && Objects.equals(String.valueOf(character.getAccountID()), data.getExtendId())) {
                    return character;
                }

                if (ExtendType.isCharacter(data.getExtendType()) && Objects.equals(String.valueOf(character.getId()), data.getExtendId())) {
                    return character;
                }
            }
        }

        throw new IllegalArgumentException(I18nUtil.getExceptionMessage("CharacterService.getCharacter.exception1"));
    }
}
