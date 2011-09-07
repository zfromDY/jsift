/*
 * Copyright 2011 Stefan C. Mueller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smurn.jsift;

import org.junit.matchers.JUnitMatchers;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link Octave}.
 */
public class OctaveTest {

    private static class MockImage extends Image {

        private final double sigma;

        public MockImage(final double sigma) {
            super(10, 10);
            this.sigma = sigma;
        }

        public double getSigma() {
            return sigma;
        }

        @Override
        public Image subtract(Image subtrahend) {
            MockImage mock = (MockImage) subtrahend;
            return new MockImage(3 * this.sigma + 2 * mock.sigma);
        }
    }

    private static class MockFilter implements LowPassFilter {

        @Override
        public Image filter(Image image, double sigma) {
            MockImage mock = (MockImage) image;
            return new MockImage((2 * mock.sigma + sigma) / 2);
        }

        @Override
        public double sigmaDifference(double sigmaFrom, double sigmaTo) {
            return 2 * (sigmaTo - sigmaFrom);
        }
    }

    @Test(expected = NullPointerException.class)
    public void ctrNullScales() {
        new Octave(null, new ArrayList<Image>());
    }

    @Test(expected = NullPointerException.class)
    public void ctrNullDoG() {
        new Octave(new ArrayList<Image>(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctrWrongCount() {
        new Octave(Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)),
                Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctrToFew() {
        new Octave(Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)),
                Arrays.asList(
                new Image(20, 20),
                new Image(20, 20)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctrWrongSize() {
        new Octave(Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)),
                Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 19)));
    }

    @Test(expected = NullPointerException.class)
    public void createImageNull() {
        Octave.create(null, 3, 1.7, mock(LowPassFilter.class));
    }

    @Test(expected = NullPointerException.class)
    public void createFilterNull() {
        Octave.create(new Image(10, 10), 3, 1.7, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createZeroScales() {
        Octave.create(new Image(10, 10), 0, 1.7, mock(LowPassFilter.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createZeroBlur() {
        Octave.create(new Image(10, 10), 3, 0, mock(LowPassFilter.class));
    }

    @Test
    public void createScaleImages1() {
        LowPassFilter filter = new MockFilter();
        Octave octave = Octave.create(new MockImage(1.5), 1, 1.5, filter);
        assertEquals(1, octave.getScalesPerOctave());
        assertEquals(1.5, ((MockImage) (octave.getScaleImages().get(0))).getSigma(), 1E-6);
        assertEquals(3.0, ((MockImage) (octave.getScaleImages().get(1))).getSigma(), 1E-6);
        assertEquals(6.0, ((MockImage) (octave.getScaleImages().get(2))).getSigma(), 1E-6);
        assertEquals(12.0, ((MockImage) (octave.getScaleImages().get(3))).getSigma(), 1E-6);
    }

    @Test
    public void createScaleImages3() {
        LowPassFilter filter = new MockFilter();
        Octave octave = Octave.create(new MockImage(1.5), 3, 1.5, filter);
        assertEquals(3, octave.getScalesPerOctave());
        assertEquals(1.5 * Math.pow(2, 0.0 / 3.0), ((MockImage) (octave.getScaleImages().get(0))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 1.0 / 3.0), ((MockImage) (octave.getScaleImages().get(1))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 2.0 / 3.0), ((MockImage) (octave.getScaleImages().get(2))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 3.0 / 3.0), ((MockImage) (octave.getScaleImages().get(3))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 4.0 / 3.0), ((MockImage) (octave.getScaleImages().get(3))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 5.0 / 3.0), ((MockImage) (octave.getScaleImages().get(4))).getSigma(), 1E-6);
    }

    @Test
    public void createDoG1() {
        LowPassFilter filter = new MockFilter();
        Octave octave = Octave.create(new MockImage(1.5), 1, 1.5, filter);
        assertEquals(1, octave.getScalesPerOctave());
        assertEquals(1.5 + 3.0, ((MockImage) (octave.getDifferenceOfGaussians().get(0))).getSigma(), 1E-6);
        assertEquals(3.0 + 6.0, ((MockImage) (octave.getDifferenceOfGaussians().get(1))).getSigma(), 1E-6);
        assertEquals(6.0 + 12.0, ((MockImage) (octave.getDifferenceOfGaussians().get(2))).getSigma(), 1E-6);
    }

    @Test
    public void createDoG3() {
        LowPassFilter filter = new MockFilter();
        Octave octave = Octave.create(new MockImage(1.5), 3, 1.5, filter);
        assertEquals(3, octave.getScalesPerOctave());
        assertEquals(1.5 * Math.pow(2, 0.0 / 3.0) + 1.5 * Math.pow(2, 1.0 / 3.0), ((MockImage) (octave.getDifferenceOfGaussians().get(0))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 1.0 / 3.0) + 1.5 * Math.pow(2, 2.0 / 3.0), ((MockImage) (octave.getDifferenceOfGaussians().get(1))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 2.0 / 3.0) + 1.5 * Math.pow(2, 3.0 / 3.0), ((MockImage) (octave.getDifferenceOfGaussians().get(2))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 3.0 / 3.0) + 1.5 * Math.pow(2, 4.0 / 3.0), ((MockImage) (octave.getDifferenceOfGaussians().get(3))).getSigma(), 1E-6);
        assertEquals(1.5 * Math.pow(2, 4.0 / 3.0) + 1.5 * Math.pow(2, 5.0 / 3.0), ((MockImage) (octave.getDifferenceOfGaussians().get(3))).getSigma(), 1E-6);
    }

    @Test
    public void getScalesPerOctave() {
        Octave target = new Octave(Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)),
                Arrays.asList(
                new Image(20, 20),
                new Image(20, 20),
                new Image(20, 20)));
        assertEquals(1, target.getScalesPerOctave());
    }

    @Test
    public void getScaleImages() {
        Octave target = new Octave(Arrays.asList(
                new Image(new float[][]{{1}}),
                new Image(new float[][]{{2}}),
                new Image(new float[][]{{3}}),
                new Image(new float[][]{{4}})),
                Arrays.asList(
                new Image(new float[][]{{5}}),
                new Image(new float[][]{{6}}),
                new Image(new float[][]{{7}})));

        assertThat(target.getScaleImages(), JUnitMatchers.hasItems(
                TestUtils.equalTo(new Image(new float[][]{{1}}), 1E-4f),
                TestUtils.equalTo(new Image(new float[][]{{2}}), 1E-4f),
                TestUtils.equalTo(new Image(new float[][]{{3}}), 1E-4f),
                TestUtils.equalTo(new Image(new float[][]{{4}}), 1E-4f)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void scaleImagesImmutable() {
        Octave target = new Octave(Arrays.asList(
                new Image(new float[][]{{1}}),
                new Image(new float[][]{{2}}),
                new Image(new float[][]{{3}}),
                new Image(new float[][]{{4}})),
                Arrays.asList(
                new Image(new float[][]{{5}}),
                new Image(new float[][]{{6}}),
                new Image(new float[][]{{7}})));
        target.getScaleImages().clear();
    }

    @Test
    public void getDoG() {
        Octave target = new Octave(Arrays.asList(
                new Image(new float[][]{{1}}),
                new Image(new float[][]{{2}}),
                new Image(new float[][]{{3}}),
                new Image(new float[][]{{4}})),
                Arrays.asList(
                new Image(new float[][]{{5}}),
                new Image(new float[][]{{6}}),
                new Image(new float[][]{{7}})));

        assertThat(target.getDifferenceOfGaussians(), JUnitMatchers.hasItems(
                TestUtils.equalTo(new Image(new float[][]{{5}}), 1E-4f),
                TestUtils.equalTo(new Image(new float[][]{{6}}), 1E-4f),
                TestUtils.equalTo(new Image(new float[][]{{7}}), 1E-4f)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void dogImmutable() {
        Octave target = new Octave(Arrays.asList(
                new Image(new float[][]{{1}}),
                new Image(new float[][]{{2}}),
                new Image(new float[][]{{3}}),
                new Image(new float[][]{{4}})),
                Arrays.asList(
                new Image(new float[][]{{5}}),
                new Image(new float[][]{{6}}),
                new Image(new float[][]{{7}})));
        target.getDifferenceOfGaussians().clear();
    }
}
