package ru.apache_maven;

import java.util.Objects;

public class Coordinates {
    public final File file;
    public final Integer rank;

    public Coordinates(File file, int rank) {
        this.file = file;
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Coordinates that = (Coordinates) o;
        
        if (file != that.file) return false;
        return rank.equals(that.rank);
    }

    @Override
    public int hashCode() {
        int result = file.hashCode();
        result = 31 + result + rank.hashCode();
        return result;
    }
}
