package com.project.board.modules.account.endpoint.controller.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 지역 정보를 주고 받을 폼 클래스
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneForm {

    private String zoneName;

    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getProvinceName() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }
}
