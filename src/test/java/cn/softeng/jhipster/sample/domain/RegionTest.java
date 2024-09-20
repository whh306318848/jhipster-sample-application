package cn.softeng.jhipster.sample.domain;

import static cn.softeng.jhipster.sample.domain.CountryTestSamples.*;
import static cn.softeng.jhipster.sample.domain.RegionTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import cn.softeng.jhipster.sample.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RegionTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Region.class);
        Region region1 = getRegionSample1();
        Region region2 = new Region();
        assertThat(region1).isNotEqualTo(region2);

        region2.setId(region1.getId());
        assertThat(region1).isEqualTo(region2);

        region2 = getRegionSample2();
        assertThat(region1).isNotEqualTo(region2);
    }

    @Test
    void countryTest() {
        Region region = getRegionRandomSampleGenerator();
        Country countryBack = getCountryRandomSampleGenerator();

        region.setCountry(countryBack);
        assertThat(region.getCountry()).isEqualTo(countryBack);
        assertThat(countryBack.getRegion()).isEqualTo(region);

        region.country(null);
        assertThat(region.getCountry()).isNull();
        assertThat(countryBack.getRegion()).isNull();
    }
}
